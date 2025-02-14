package io.ortis.jsak.concurrency.bus;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class MessageStreamBusTest
{


	@Test
	public void test() throws Exception
	{
		final ExecutorService executorService = Executors.newFixedThreadPool(16);
		final MessageStreamBus<Long> stream = new MessageStreamBus<>(executorService);
		final int max = 2_000_000;
		final int mid = max / 2;

		final List<AtomicLong> counters = new ArrayList<>(31);
		for (int i = 0; i < 31; i++)
		{
			counters.add(new AtomicLong(0));
			final int index = i;
			stream.subscribe((bus, message) -> {
				counters.get(index).addAndGet(message);
			});
		}


		final Thread t1 = new Thread(() -> {
			for (long i = 0; i < mid; i++)
			{
				//System.out.println(i);
				stream.awaitPublish(i);
			}

		});

		final Thread t2 = new Thread(() -> {
			for (long i = mid; i < max; i++)
				stream.asyncPublish(i);

		});

		t2.start();
		t1.start();

		t2.join();
		t1.join();
		executorService.shutdown();
		Assertions.assertTrue(executorService.awaitTermination(1, TimeUnit.MINUTES));

		for (final AtomicLong counter : counters)
			Assertions.assertEquals((long) max * (max - 1) / 2, counter.get());


	}


}
