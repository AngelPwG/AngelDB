package main.java.tests;

import btree.LeafNode;
import storage.DiskManager;

public class WriteAndReadTest {
    public static void main(String[] args) {

        DiskManager diskManager = new DiskManager("angelDB.db");
        byte[] data = diskManager.readPage(0);

        LeafNode node = diskManager.deserializeLeaf(data);

        System.out.println("Data written from the disk: " + node.data.getFirst());
        /*
        LeafNode node = new LeafNode(39);
        node.keys.add(1L);
        node.data.add(new Row(1L, "Jose Angel", 20));

        byte[] data = diskManager.serializeLeaf(node);

        diskManager.writePage(0, data);

        System.out.println("Data written to disk.");
        */

    }
}
