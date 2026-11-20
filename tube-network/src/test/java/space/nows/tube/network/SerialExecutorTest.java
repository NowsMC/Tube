package space.nows.tube.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class SerialExecutorTest {

    @Test
    void tasksRunOneAtATimeInTheOrderTheyCame() throws InterruptedException {
        try (ExecutorService backing = Executors.newVirtualThreadPerTaskExecutor()) {
            SerialExecutor queue = new SerialExecutor(backing);
            List<Integer> seen = new ArrayList<>();
            AtomicInteger running = new AtomicInteger();
            AtomicInteger overlaps = new AtomicInteger();
            CountDownLatch done = new CountDownLatch(10_000);
            for (int i = 0; i < 10_000; i++) {
                int number = i;
                queue.execute(() -> {
                    if (running.incrementAndGet() > 1) {
                        overlaps.incrementAndGet();
                    }
                    seen.add(number);
                    running.decrementAndGet();
                    done.countDown();
                });
            }
            assertTrue(done.await(10, TimeUnit.SECONDS));
            assertEquals(0, overlaps.get());
            for (int i = 0; i < 10_000; i++) {
                assertEquals(i, seen.get(i));
            }
            assertEquals(0, queue.queued());
        }
    }

    @Test
    void aFailingTaskDoesNotStopTheOnesAfterIt() throws InterruptedException {
        try (ExecutorService backing = Executors.newVirtualThreadPerTaskExecutor()) {
            SerialExecutor queue = new SerialExecutor(backing);
            CountDownLatch after = new CountDownLatch(1);
            queue.execute(() -> {
                throw new IllegalStateException("plugin bug");
            });
            queue.execute(after::countDown);
            assertTrue(after.await(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void tasksAreDroppedQuietlyOnceTheBackingExecutorIsShutDown() {
        ExecutorService backing = Executors.newVirtualThreadPerTaskExecutor();
        backing.shutdown();
        SerialExecutor queue = new SerialExecutor(backing);

        queue.execute(() -> {
        });

        assertEquals(0, queue.queued());
    }
}
