package main.java.tests;

import btree.BPlusTree;
import storage.BufferPool;
import storage.DiskManager;

public class IntegrationTest {
    public static void main(String[] args) {
        DiskManager diskManager = new DiskManager("angel.db");

        // m = 77, t = m / 2  - 1= 39.~
        BPlusTree tree = new BPlusTree(39, new BufferPool(5, diskManager));

        /*
            System.out.println("--- STARTING INSERTION ---");
            long start = System.currentTimeMillis();

            int total = 400;
            for (int i = 1; i <= total; i++) {
                tree.insert(i, new Row(i, "User " + i, 20 + (i % 50)));
                if (i % 100 == 0) System.out.println("Inserted " + i + " records...");
            }

            long end = System.currentTimeMillis();
            System.out.println("--- INSERTION COMPLETE (" + (end - start) + "ms) ---");
         */
        System.out.println(tree.search(348));
        System.out.println("--- FINAL TREE STRUCTURE ---");
        tree.printStructure();
    }
}
