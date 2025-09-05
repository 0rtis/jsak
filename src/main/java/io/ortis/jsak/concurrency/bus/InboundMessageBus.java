package io.ortis.jsak.concurrency.bus;

import io.ortis.jsak.lang.Pointer;

import java.time.Duration;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface InboundMessageBus<M>
{
	default M poll()
	{
		return poll(null);
	}

	default M poll(final Duration timeout)
	{
		final Pointer<M> pointer = new Pointer<>(null);
		poll(timeout, 1, pointer::setValue);
		return pointer.getValue();
	}

	int poll(final Duration timeout, final int limit, final Consumer<M> destination);

	void ack(final M message);

	boolean subscribe(final InboundMessageBusListener<M> listener);

	boolean unsubscribe(final InboundMessageBusListener<M> listener);
}
