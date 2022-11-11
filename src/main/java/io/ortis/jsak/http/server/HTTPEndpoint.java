package io.ortis.jsak.http.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface HTTPEndpoint
{
	public static final String HTTP_METHOD_GET = "GET";
	public static final String HTTP_METHOD_POST = "POST";
	public static final String HTTP_METHOD_OPTIONS = "OPTIONS";
	public static final String HTTP_METHOD_PUT = "PUT";
	public static final String HTTP_METHOD_DELETE = "DELETE";


	public static final Locale DEFAULT_LOCAL = Locale.ENGLISH;

	boolean isMatch(final String requestMethod, final Map<String, List<String>> requestHeaders, final String path);

	Response respond(final InetSocketAddress remoteHost, final String requestMethod, final Map<String, List<String>> requestHeaders,
			final String path,
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
		public static final Map<String, String> EMPTY_HEADERS = Map.of();

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


		public static Builder newBuilder()
		{
			return new Builder();
		}

		public static Builder newBuilder(final Response copy)
		{
			Builder builder = new Builder();
			builder.headers = copy.getHeaders();
			builder.code = copy.getCode();
			builder.payloadLength = copy.getPayloadLength();
			builder.payload = copy.getPayload();
			builder.compressible = copy.isCompressible();
			builder.listener = copy.listener;
			return builder;
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
			return http204NoContent(EMPTY_HEADERS);
		}

		public static Response http204NoContent(final Map<String, String> headers)
		{
			return new Response(headers, 204, 0, new ByteArrayInputStream(new byte[0]), false);
		}

		public static Response http400BadRequest()
		{
			return http400BadRequest((String) null);
		}

		public static Response http400BadRequest(final Map<String, String> headers)
		{
			return http400BadRequest(headers, (String) null);
		}

		public static Response http400BadRequest(final String msg)
		{
			return http400BadRequest(EMPTY_HEADERS, msg);
		}

		public static Response http400BadRequest(final Map<String, String> headers, String msg)
		{
			if (msg == null)
				msg = "Bad request";
			final ErrorPayload errorPayload = new ErrorPayload(msg);
			return http400BadRequest(headers, GSON.toJson(errorPayload).getBytes(StandardCharsets.UTF_8));
		}

		public static Response http400BadRequest(final Map<String, String> headers, final byte[] payload)
		{
			if (payload == null)
				return http400BadRequest(headers);

			return new Response(headers, 400, payload.length, new ByteArrayInputStream(payload), false);
		}


		/**
		 * Invalid credentials
		 *
		 * @return
		 */
		public static Response http401Unauthorized()
		{
			return http401Unauthorized((String) null);
		}

		public static Response http401Unauthorized(final Map<String, String> headers)
		{
			return http401Unauthorized(headers, (String) null);
		}

		public static Response http401Unauthorized(final String msg)
		{
			return http401Unauthorized(EMPTY_HEADERS, msg);
		}


		/**
		 * Invalid credentials
		 *
		 * @param msg
		 * @return
		 */
		public static Response http401Unauthorized(final Map<String, String> headers, String msg)
		{
			if (msg == null)
				msg = "Unauthorized";
			final ErrorPayload errorPayload = new ErrorPayload(msg);
			return http401Unauthorized(headers, GSON.toJson(errorPayload).getBytes(StandardCharsets.UTF_8));
		}

		/**
		 * Invalid credentials
		 *
		 * @param payload
		 * @return
		 */
		public static Response http401Unauthorized(final Map<String, String> headers, final byte[] payload)
		{
			if (payload == null)
				return http401Unauthorized(headers);

			return new Response(headers, 401, payload.length, new ByteArrayInputStream(payload), false);
		}


		/**
		 * Credentials provided but enough rights to perform the action
		 *
		 * @return
		 */

		public static Response http403Forbidden()
		{
			return http403Forbidden((String) null);
		}

		public static Response http403Forbidden(final Map<String, String> headers)
		{
			return http403Forbidden(headers, (String) null);
		}

		public static Response http403Forbidden(final String msg)
		{
			return http403Forbidden(EMPTY_HEADERS, msg);
		}


		/**
		 * Credentials provided but enough rights to perform the action
		 *
		 * @param msg
		 * @return
		 */
		public static Response http403Forbidden(final Map<String, String> headers, String msg)
		{
			if (msg == null)
				msg = "Forbidden";
			final ErrorPayload errorPayload = new ErrorPayload(msg);
			return http403Forbidden(headers, GSON.toJson(errorPayload).getBytes(StandardCharsets.UTF_8));
		}

		/**
		 * Credentials provided but enough rights to perform the action
		 *
		 * @param payload
		 * @return
		 */
		public static Response http403Forbidden(final Map<String, String> headers, final byte[] payload)
		{
			if (payload == null)
				return http403Forbidden(headers);

			return new Response(headers, 403, payload.length, new ByteArrayInputStream(payload), false);
		}

		public static Response http404NotFound()
		{
			return http404NotFound((String) null);
		}

		public static Response http404NotFound(final Map<String, String> headers)
		{
			return http404NotFound(headers, (String) null);
		}

		public static Response http404NotFound(final String msg)
		{
			return http404NotFound(EMPTY_HEADERS, msg);
		}

		public static Response http404NotFound(final Map<String, String> headers, String msg)
		{
			if (msg == null)
				msg = "Resource not found";
			final ErrorPayload errorPayload = new ErrorPayload(msg);
			return http404NotFound(headers, GSON.toJson(errorPayload).getBytes(StandardCharsets.UTF_8));
		}

		public static Response http404NotFound(final Map<String, String> headers, final byte[] payload)
		{
			if (payload == null)
				return http404NotFound(headers);

			return new Response(headers, 404, payload.length, new ByteArrayInputStream(payload), false);
		}

		public static Response http405MethodNotAllowed()
		{
			return http405MethodNotAllowed((String) null);
		}

		public static Response http405MethodNotAllowed(final Map<String, String> headers)
		{
			return http405MethodNotAllowed(headers, (String) null);
		}

		public static Response http405MethodNotAllowed(final String msg)
		{
			return http405MethodNotAllowed(EMPTY_HEADERS, msg);
		}

		public static Response http405MethodNotAllowed(final Map<String, String> headers, String msg)
		{
			if (msg == null)
				msg = "Method not allowed";
			final ErrorPayload errorPayload = new ErrorPayload(msg);
			return http405MethodNotAllowed(headers, GSON.toJson(errorPayload).getBytes(StandardCharsets.UTF_8));
		}

		public static Response http405MethodNotAllowed(final Map<String, String> headers, final byte[] payload)
		{
			if (payload == null)
				return http405MethodNotAllowed(headers);

			return new Response(headers, 405, payload.length, new ByteArrayInputStream(payload), false);
		}

		public static Response http429TooManyRequests()
		{
			return http429TooManyRequests((String) null);
		}

		public static Response http429TooManyRequests(final Map<String, String> headers)
		{
			return http429TooManyRequests(headers, (String) null);
		}

		public static Response http429TooManyRequests(final String msg)
		{
			return http429TooManyRequests(EMPTY_HEADERS, msg);
		}

		public static Response http429TooManyRequests(final Map<String, String> headers, String msg)
		{
			if (msg == null)
				msg = "Too many requests";
			final ErrorPayload errorPayload = new ErrorPayload(msg);
			return http429TooManyRequests(headers, GSON.toJson(errorPayload).getBytes(StandardCharsets.UTF_8));
		}

		public static Response http429TooManyRequests(final Map<String, String> headers, final byte[] payload)
		{
			if (payload == null)
				return http429TooManyRequests(headers);

			return new Response(headers, 429, payload.length, new ByteArrayInputStream(payload), false);
		}


		public static Response http500InternalError()
		{
			return http500InternalError((String) null);
		}

		public static Response http500InternalError(final Map<String, String> headers)
		{
			return http500InternalError(headers, (String) null);
		}

		public static Response http500InternalError(final String msg)
		{
			return http500InternalError(EMPTY_HEADERS, msg);
		}

		public static Response http500InternalError(final Map<String, String> headers, String msg)
		{
			if (msg == null)
				msg = "Internal error";
			final ErrorPayload errorPayload = new ErrorPayload(msg);
			return http500InternalError(headers, GSON.toJson(errorPayload).getBytes(StandardCharsets.UTF_8));
		}

		public static Response http500InternalError(final Map<String, String> headers, final byte[] payload)
		{
			if (payload == null)
				return http500InternalError(headers);

			return new Response(headers, 500, payload.length, new ByteArrayInputStream(payload), false);
		}

		public static Response http503ServiceNotAvailable()
		{
			return http503ServiceNotAvailable((String) null);
		}

		public static Response http503ServiceNotAvailable(final Map<String, String> headers)
		{
			return http503ServiceNotAvailable(headers, (String) null);
		}

		public static Response http503ServiceNotAvailable(final String msg)
		{
			return http503ServiceNotAvailable(EMPTY_HEADERS, msg);
		}

		public static Response http503ServiceNotAvailable(final Map<String, String> headers, String msg)
		{
			if (msg == null)
				msg = "Service not available";
			final ErrorPayload errorPayload = new ErrorPayload(msg);
			return http503ServiceNotAvailable(headers, GSON.toJson(errorPayload).getBytes(StandardCharsets.UTF_8));
		}

		public static Response http503ServiceNotAvailable(final Map<String, String> headers, final byte[] payload)
		{
			if (payload == null)
				return http503ServiceNotAvailable(headers);

			return new Response(headers, 503, payload.length, new ByteArrayInputStream(payload), false);
		}

		public static interface Listener
		{
			void onCompleted(final Response response);
		}


		public static final class Builder
		{
			private Map<String, String> headers;
			private int code;
			private long payloadLength;
			private InputStream payload;
			private boolean compressible;
			private Listener listener;

			private Builder()
			{
			}

			public Builder headers(final Map<String, String> val)
			{
				headers = val;
				return this;
			}

			public Builder addHeaders(final Map<String, String> val)
			{
				if (val == null)
					return this;

				if (headers == null)
					headers = Map.of();

				headers = Stream.of(headers, val).flatMap(map -> map.entrySet().stream())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2));
				return this;
			}

			public Builder code(final int val)
			{
				code = val;
				return this;
			}

			public Builder payloadLength(final long val)
			{
				payloadLength = val;
				return this;
			}

			public Builder payload(final InputStream val)
			{
				payload = val;
				return this;
			}

			public Builder compressible(final boolean val)
			{
				compressible = val;
				return this;
			}

			public Builder listener(final Listener val)
			{
				listener = val;
				return this;
			}

			public Response build()
			{
				return new Response(headers, code, payloadLength, payload, compressible, listener);
			}
		}
	}

}


