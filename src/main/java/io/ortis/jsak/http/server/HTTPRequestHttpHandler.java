package io.ortis.jsak.http.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.ortis.jsak.http.server.config.HTTPServerConfig;
import io.ortis.jsak.io.Compression;
import io.ortis.jsak.io.IOUtils;
import io.ortis.jsak.http.server.limiter.HTTPLimiter;
import io.ortis.jsak.FormatUtils;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

@SuppressWarnings("restriction")
public class HTTPRequestHttpHandler implements HttpHandler
{
	private final String contextPath;
	private final String contextPathUpperCase;
	private final HTTPServerConfig config;
	private final List<HTTPEndpoint> endpoints;
	private final Compression.Algorithm compressionAlgorithm;
	private final HTTPLimiter limiter;
	private final int bufferLength;
	private final Logger log;

	public HTTPRequestHttpHandler(final HTTPServerConfig config, final List<HTTPEndpoint> endpoints,
			final Compression.Algorithm compressionAlgorithm, final HTTPLimiter limiter, final int bufferLength,
			final Logger log)
	{
		this(null, config, endpoints, compressionAlgorithm, limiter, bufferLength, log);
	}

	public HTTPRequestHttpHandler(final String contextPath, final HTTPServerConfig config, final List<HTTPEndpoint> endpoints,
			final Compression.Algorithm compressionAlgorithm, final HTTPLimiter limiter, final int bufferLength,
			final Logger log)
	{
		this.contextPath = contextPath;
		this.contextPathUpperCase =
				contextPath == null || contextPath.equals("/") ? "" : this.contextPath.toUpperCase(Locale.ENGLISH);

		this.config = config;
		this.endpoints = List.copyOf(endpoints);
		this.compressionAlgorithm = compressionAlgorithm;
		this.limiter = limiter;
		this.bufferLength = bufferLength;
		this.log = log;
	}

	@Override
	public void handle(final HttpExchange httpExchange)
	{
		final byte[] buffer = new byte[this.bufferLength];

		final String rawPath = httpExchange.getRequestURI().getPath();
		final String rawUpperPath = rawPath.toUpperCase(Locale.ENGLISH);

		final String upperPath = rawUpperPath.substring(this.contextPathUpperCase.length()).toUpperCase(Locale.ENGLISH);
		final String path = rawPath.substring(rawPath.length() - upperPath.length());
		final String query = httpExchange.getRequestURI().getQuery();

		HTTPEndpoint.Response response;

		final Map<String, List<String>> requestHeaders = new LinkedHashMap<>();
		try
		{
			requestHeaders.putAll(httpExchange.getRequestHeaders());

			final String rejectReason;
			{
				final List<String> passList = this.config.getPassList();
				final List<String> banList = this.config.getBanList();

				final String host = httpExchange.getRemoteAddress().getAddress().getHostAddress();
				this.log.finer("Request from " + host + " - " + rawPath);

				if (passList != null && (passList.contains(host) || passList.contains("*")))
					rejectReason = null;
				else if (banList != null && (banList.contains(host) || banList.contains("*")))
					rejectReason = "Banned";
				else
					rejectReason = this.limiter.onRequest(host, System.currentTimeMillis());
			}


			if (rejectReason == null)
			{
				if (httpExchange.getRequestMethod().equals("OPTIONS"))
				{
					final Map<String, String> header = new HashMap<>();
					header.put("Access-Control-Allow-Origin", "*");
					header.put("Access-Control-Allow-Headers",
							"origin, authorization, x-requested-with, content-type, accept, access-control-allow-origin");
					header.put("Access-Control-Allow-Methods", "GET, OPTIONS, HEAD, PUT, POST");
					response = new HTTPEndpoint.Response(header, 200, new byte[0], false);
				} else
				{
					final HTTPEndpoint endpoint;
					{
						HTTPEndpoint match = null;
						for (final HTTPEndpoint httpe : this.endpoints)
							if (httpe.isMatch(httpExchange.getRequestMethod(), requestHeaders, upperPath))
							{
								match = httpe;
								break;
							}

						endpoint = match;
					}

					if (endpoint == null)
						response = HTTPEndpoint.Response.http404NotFound();
					else
						response = endpoint.respond(httpExchange.getRequestMethod(), requestHeaders, path, query,
								httpExchange.getRequestBody());
				}
			} else
				response = new HTTPEndpoint.Response(HTTPEndpoint.Response.EMPTY_HEADERS, 429,
						rejectReason.getBytes(StandardCharsets.UTF_8), false);

		} catch (final Exception e)
		{
			this.log.severe("Error while processing request - " + FormatUtils.formatException(e));
			response = HTTPEndpoint.Response.http500InternalError(FormatUtils.formatExceptionMessage(e));
		}

		try
		{
			response.getHeaders().forEach((k, v) -> httpExchange.getResponseHeaders().add(k, v));
			for (final Map.Entry<String, String> header : this.config.getIncludeHttpResponseHeaders().entrySet())
				httpExchange.getResponseHeaders().set(header.getKey(), header.getValue());

			respond:
			if (response.isCompressible())
			{
				switch (this.compressionAlgorithm)
				{
					case Gzip:

						gzip:
						{// check gzip support
							for (final Map.Entry<String, List<String>> header : requestHeaders.entrySet())
								if (header.getKey().toUpperCase(Locale.ENGLISH).trim().equals("ACCEPT-ENCODING"))
								{
									for (final String encoding : header.getValue())
										if (encoding.trim().toUpperCase(Locale.ENGLISH).contains("GZIP"))
											break gzip;
								}

							final HTTPEndpoint.Response gzipMissing =
									new HTTPEndpoint.Response(HTTPEndpoint.Response.EMPTY_HEADERS,
											406 /* Not acceptable */, HTTPEndpoint.Response.GSON
											.toJson(new HTTPEndpoint.ErrorPayload("Client must accept gzip encoding"))
											.getBytes(StandardCharsets.UTF_8), false);
							sendResponse(gzipMissing.getCode(), gzipMissing.getPayload(),
									gzipMissing.getPayloadLength(), buffer, httpExchange);
							break respond;
						}

						httpExchange.getResponseHeaders().add("Content-Encoding", "gzip");
						break;

					case Raw:
						break;

					default:
						throw new RuntimeException("Unhandled compression algo " + this.compressionAlgorithm);
				}

				final byte[] compressedPayload;
				try (final ByteArrayOutputStream baos = new ByteArrayOutputStream())
				{
					Compression.deflate(this.compressionAlgorithm, response.getPayload(), baos, buffer);
					compressedPayload = baos.toByteArray();
				}

				try (final ByteArrayInputStream bais = new ByteArrayInputStream(compressedPayload))
				{
					sendResponse(response.getCode(), bais, compressedPayload.length, buffer, httpExchange);
				}
			} else
				sendResponse(response.getCode(), response.getPayload(), response.getPayloadLength(), buffer,
						httpExchange);


			if (response.getPayloadLength() > 4 * 1024 * 1024)
				System.gc();

		} catch (Exception e)
		{
			if (!FormatUtils.formatExceptionMessage(e).contains("An established connection was aborted"))
				this.log.severe(FormatUtils.formatException(e));
		}
	}

	private void sendResponse(final int httpCode, final InputStream inputStream, final long length, final byte[] buffer,
			final HttpExchange httpExchange) throws IOException
	{
		httpExchange.sendResponseHeaders(httpCode, length);
		try (final OutputStream os = httpExchange.getResponseBody())
		{
			IOUtils.stream(inputStream, os, buffer);
			os.flush();
		}
	}

	private static String getBlockName(final BigInteger height)
	{
		final StringBuilder sb = new StringBuilder(height.toString(16));
		while (sb.length() < 16)
			sb.insert(0, "0");
		sb.insert(0, "b");
		sb.append(".bc");
		return sb.toString();
	}
}
