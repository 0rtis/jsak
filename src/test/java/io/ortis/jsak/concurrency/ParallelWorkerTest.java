package io.ortis.jsak.concurrency;

import io.ortis.jsak.concurrency.bus.MessageStreamBus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class ParallelWorkerTest
{


	@Test
	public void test() throws Exception
	{
		final AtomicLong counter = new AtomicLong(0);
		final BlockingQueue<Long> queue = new LinkedBlockingQueue<>();
		final ParallelWorker<Long> worker = ParallelWorker.<Long>newBuilder()
														  .setParallelism(8)
														  .setSupplier(queue, Duration.ofSeconds(1))
														  .setConsumer(counter::addAndGet)
														  .setThreadFactory(Threads.buildPlainThreadFactory(ParallelWorkerTest.class, false))
														  .setExceptionHandler(Throwable::printStackTrace)
														  .build();
		final int max = 2_000_000;
		final int mid = max / 2;


		final Thread t1 = new Thread(() -> {
			for (long i = 0; i < mid; i++)
				queue.add(i);

		});

		final Thread t2 = new Thread(() -> {
			for (long i = mid; i < max; i++)
				queue.add(i);

		});

		t2.start();
		t1.start();

		t2.join();
		t1.join();
		worker.shutdown();
		Assertions.assertTrue(worker.awaitTermination(Duration.ofMinutes(1)));
		Assertions.assertEquals((long) max * (max - 1) / 2, counter.get());


	}


}
