package io.ortis.jsak.concurrency.bus;

public interface InboundMessageBusListener<M>
{
	void onMessage(final InboundMessageBus<M> bus, final M message);
}
