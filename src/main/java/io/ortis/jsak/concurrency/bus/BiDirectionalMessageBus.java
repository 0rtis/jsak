package io.ortis.jsak.concurrency.bus;

public interface BiDirectionalMessageBus<M, I> extends InboundMessageBus<M>, OutboundMessageBus<M, I>
{
}
