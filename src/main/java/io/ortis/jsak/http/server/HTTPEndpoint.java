package io.ortis.jsak.http.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public interface HTTPEndpoint
{
	public static final String HTTP_METHOD_GET = "GET";
	public static final String HTTP_METHOD_POST = "POST";
	public static final Locale DEFAULT_LOCAL = Locale.ENGLISH;

	boolean isMatch(final String requestMethod, final Map<String, List<String>> requestHeaders, final String path);

	Response respond(final String requestMethod, final Map<String, List<String>> requestHeaders, final String path,
					 final String query, final InputStream requestBody);

	public static class ErrorPayload
	{
		public String error;

		public ErrorPayload(final String error)
		{
			this.error = error;
		}
	}

	public static class Response
	{
		public static final Map<String, String> EMPTY_HEADERS = Collections.unmodifiableMap(new HashMap<>());

		public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

		private final Map<String, String> headers;
		private final int code;
		private final long payloadLength;
		private final InputStream payload;
		private final boolean compressible;
		private final Listener listener;

		public Response(final Map<String, String> headers, final int code, final byte[] payload,
						final boolean compressible)
		{
			this(headers, code, payload.length, new ByteArrayInputStream(payload), compressible, null);
		}

		public Response(final Map<String, String> headers, final int code, final long payloadLength,
						final InputStream payload, final boolean compressible)
		{
			this(headers, code, payloadLength, payload, compressible, null);
		}

		public Response(final Map<String, String> headers, final int code, final byte[] payload,
						final boolean compressible, final Listener listener)
		{
			this(headers, code, payload.length, new ByteArrayInputStream(payload), compressible, listener);
		}

		public Response(final Map<String, String> headers, final int code, final long payloadLength,
						final InputStream payload, final boolean compressible, final Listener listener)
		{
			this.headers = headers == null ? EMPTY_HEADERS : headers;
			this.code = code;
			this.payloadLength = payloadLength;
			this.payload = payload;
			this.compressible = compressible;
			this.listener = listener;
		}

		public void cleanUp()
		{
			if (this.listener != null)
				this.listener.onCompleted(this);
		}

		public Map<String, String> getHeaders()
		{
			return this.headers;
		}

		public int getCode()
		{
			return this.code;
		}

		public long getPayloadLength()
		{
			return this.payloadLength;
		}

		public InputStream getPayload()
		{
			return this.payload;
		}

		public boolean isCompressible()
		{
			return this.compressible;
		}

		public static Response http204NoContent()
		{
			return new Response(EMPTY_HEADERS, 204, 0, new ByteArrayInputStream(new byte [0]), false);
		}

		public static Response http400BadRequest()
		{
			return http400BadRequest((String) null);
		}

		public static Response http400BadRequest(String msg)
		{
			if (msg == null)
				msg = "Bad request";
			final ErrorPayload errorPayload = new ErrorPayload(msg);
			return http400BadRequest(GSON.toJson(errorPayload).getBytes(StandardCharsets.UTF_8));
		}

		public static Response http400BadRequest(byte[] payload)
		{
			if (payload == null)
				return http400BadRequest();

			return new Response(EMPTY_HEADERS, 400, payload.length, new ByteArrayInputStream(payload), false);
		}

		public static Response http401Unauthorized()
		{
			return http401Unauthorized((String) null);
		}

		public static Response http401Unauthorized(String msg)
		{
			if (msg == null)
				msg = "Unauthorized";
			final ErrorPayload errorPayload = new ErrorPayload(msg);
			return http401Unauthorized(GSON.toJson(errorPayload).getBytes(StandardCharsets.UTF_8));
		}

		public static Response http401Unauthorized(byte[] payload)
		{
			if (payload == null)
				return http401Unauthorized();

			return new Response(EMPTY_HEADERS, 401, payload.length, new ByteArrayInputStream(payload), false);
		}

		public static Response http404NotFound()
		{
			return http404NotFound((String) null);
		}

		public static Response http404NotFound(String msg)
		{
			if (msg == null)
				msg = "Endpoint not found";
			final ErrorPayload errorPayload = new ErrorPayload(msg);
			return http404NotFound(GSON.toJson(errorPayload).getBytes(StandardCharsets.UTF_8));
		}

		public static Response http404NotFound(byte[] payload)
		{
			if (payload == null)
				return http404NotFound();

			return new Response(EMPTY_HEADERS, 404, payload.length, new ByteArrayInputStream(payload), false);
		}

		public static Response http405MethodNotAllowed()
		{
			return http405MethodNotAllowed((String) null);
		}

		public static Response http405MethodNotAllowed(String msg)
		{
			if (msg == null)
				msg = "Method not allowed";
			final ErrorPayload errorPayload = new ErrorPayload(msg);
			return http405MethodNotAllowed(GSON.toJson(errorPayload).getBytes(StandardCharsets.UTF_8));
		}

		public static Response http405MethodNotAllowed(byte[] payload)
		{
			if (payload == null)
				return http405MethodNotAllowed();

			return new Response(EMPTY_HEADERS, 405, payload.length, new ByteArrayInputStream(payload), false);
		}

		public static Response http429TooManyRequests()
		{
			return http429TooManyRequests((String) null);
		}

		public static Response http429TooManyRequests(String msg)
		{
			if (msg == null)
				msg = "Too many requests";
			final ErrorPayload errorPayload = new ErrorPayload(msg);
			return http429TooManyRequests(GSON.toJson(errorPayload).getBytes(StandardCharsets.UTF_8));
		}

		public static Response http429TooManyRequests(byte[] payload)
		{
			if (payload == null)
				return http429TooManyRequests();

			return new Response(EMPTY_HEADERS, 429, payload.length, new ByteArrayInputStream(payload), false);
		}


		public static Response http500InternalError()
		{
			return http500InternalError((String) null);
		}

		public static Response http500InternalError(String msg)
		{
			if (msg == null)
				msg = "Internal error";
			final ErrorPayload errorPayload = new ErrorPayload(msg);
			return http500InternalError(GSON.toJson(errorPayload).getBytes(StandardCharsets.UTF_8));
		}

		public static Response http500InternalError(byte[] payload)
		{
			if (payload == null)
				return http500InternalError();

			return new Response(EMPTY_HEADERS, 500, payload.length, new ByteArrayInputStream(payload), false);
		}

		public static Response http503ServiceNotAvailable()
		{
			return http503ServiceNotAvailable((String) null);
		}

		public static Response http503ServiceNotAvailable(String msg)
		{
			if (msg == null)
				msg = "Service not available";
			final ErrorPayload errorPayload = new ErrorPayload(msg);
			return http503ServiceNotAvailable(GSON.toJson(errorPayload).getBytes(StandardCharsets.UTF_8));
		}


		public static Response http503ServiceNotAvailable(byte[] payload)
		{
			if (payload == null)
				return http503ServiceNotAvailable();

			return new Response(EMPTY_HEADERS, 503, payload.length, new ByteArrayInputStream(payload), false);
		}

		public static interface Listener
		{
			void onCompleted(final Response response);
		}
	}

}


