package api.test;

import btree.BPlusTree;
import models.Row;
import storage.BufferPool;
import storage.DiskManager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConcurrencyStressTest {
    static int cores = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService executor = Executors.newFixedThreadPool(cores);

    public static void main(String[] args) {
        long numberOfTasks = 1000;

        CountDownLatch latch = new CountDownLatch(1);

        BPlusTree tree = new BPlusTree(39, new BufferPool(50, new DiskManager("angel.db")));

        int initialRecords = tree.selectAll().size();
        System.out.println("Initial Records: " + initialRecords);

        for(long i = 0; i < numberOfTasks; i++){
            long finalI = i;
            long id = i + initialRecords;
            executor.submit(() ->{
                try{
                    latch.await();
                    tree.insert(id, new Row(id, "Thread " + finalI, 20));
                } catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        System.out.println("Starting stress test...");
        latch.countDown();

        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Test finished.");
        int finalRecords = tree.selectAll().size();
        System.out.println("Expected Result: " + (initialRecords + numberOfTasks));
        System.out.println("Actual Result: " + finalRecords);
    }
}
