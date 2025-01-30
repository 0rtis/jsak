package io.ortis.jsak.concurrency.bus;

import java.time.Duration;
import java.util.concurrent.Future;

public interface OutboundMessageBus<M, I>
{
	default I awaitPublish(final M message)
	{
		return awaitPublish(message, null);
	}

	I awaitPublish(final M message, final Duration timeout);

	default Future<I> asyncPublish(final M message)
	{
		return asyncPublish(message, null);
	}

	Future<I> asyncPublish(final M message, final Duration timeout);
}
