package api.test;

import btree.BPlusTree;
import models.Row;
import storage.BufferPool;
import storage.DiskManager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MixedWorkloadTest {
    static int cores = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService executor = Executors.newFixedThreadPool(cores);

    public static void main(String[] args) {
        long numberOfTasks = 1000;

        CountDownLatch latch = new CountDownLatch(1);

        BPlusTree tree = new BPlusTree(39, new BufferPool(50, new DiskManager("angel.db")));

        AtomicInteger writeErrors = new AtomicInteger(0);
        AtomicInteger readErrors = new AtomicInteger(0);
        AtomicInteger successfulReads = new AtomicInteger(0);

        int initialRecords = tree.selectAll().size();
        int readers = 1;
        for(long i = 0; i < numberOfTasks; i++){
            long finalI = i;
            long id = readers + initialRecords;
            if (i % 10 == 0) {
                executor.submit(() -> {
                    try {
                        latch.await();
                        tree.insert(id, new Row(id, "Thread " + finalI, 20));
                    } catch (Exception e) {
                        e.printStackTrace();
                        writeErrors.incrementAndGet();
                    }
                });
                readers++;
            } else {
                executor.submit(() -> {
                    try {
                        latch.await();
                        long searchTarget = (initialRecords > 0) ? (long) (Math.random() * initialRecords) : 0;
                        Row result = tree.search(searchTarget);

                        if (initialRecords > 0 && result == null) {
                            System.err.println("ID " + searchTarget + " should exist but wasn't found.");
                            readErrors.incrementAndGet();
                        }

                        successfulReads.incrementAndGet();
                    } catch (Exception e) {
                        e.printStackTrace();
                        readErrors.incrementAndGet();
                    }
                });
            }
        }

        System.out.println("Starting mixed stress test...");
        latch.countDown();

        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Test finished.");
        int finalRecords = tree.selectAll().size();
        System.out.println("Expected Total: " + (initialRecords + (numberOfTasks * 0.1)));
        System.out.println("Actual Result:  " + finalRecords);
        System.out.println("Total Writers Launched: " + (numberOfTasks * 0.1));
        System.out.println("Total Readers Launched: " + (numberOfTasks * 0.9));
        System.out.println("Successful Reads:       " + successfulReads.get());
        System.out.println("Write Errors: " + writeErrors.get());
        System.out.println("Read Errors:  " + readErrors.get());
    }
}
