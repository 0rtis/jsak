package io.ortis.jsak.concurrency.bus;

import io.ortis.jsak.util.InputValidator;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class MessageStreamBus<M> implements BiDirectionalMessageBus<M, Void>
{
	private final Executor executor;

	private final AtomicInteger notificationPending = new AtomicInteger(0);
	private final Set<InboundMessageBusListener<M>> listeners = ConcurrentHashMap.newKeySet();

	public MessageStreamBus(final Executor executor)
	{
		this.executor = InputValidator.checkNotNull(executor, "executor");
	}

	public int getNotificationPending()
	{
		return notificationPending.get();
	}

	@Override
	public Void awaitPublish(final M message, final Duration timeout)
	{

		this.listeners.forEach(l -> {
			notificationPending.incrementAndGet();
			executor.execute(() -> {
				l.onMessage(this, message);
				notificationPending.decrementAndGet();
			});
		});

		return null;
	}

	@Override
	public Future<Void> asyncPublish(final M message, final Duration timeout)
	{
		final FutureTask<Void> future = new FutureTask<>(() -> awaitPublish(message, timeout));
		this.executor.execute(future);
		return future;
	}

	@Override
	public int poll(final Duration timeout, final int limit, final Consumer<M> destination)
	{
		return 0;
	}

	@Override
	public void ack(final M message)
	{

	}

	@Override
	public boolean subscribe(final InboundMessageBusListener<M> listener)
	{
		return this.listeners.add(listener);
	}
}
