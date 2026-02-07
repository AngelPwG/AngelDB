package storage;

import btree.BPlusNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class BufferPool {
    private HashMap<Long, BPlusNode> nodes;
    private Queue<Long> pageOrder;
    private int maxCapacity;
    private DiskManager diskManager;

    public BufferPool(int maxCapacity, DiskManager diskManager){
        this.nodes = new HashMap<>();
        this.pageOrder = new LinkedList<>();
        this.diskManager = diskManager;
        this.maxCapacity = maxCapacity;
    }

    public synchronized BPlusNode getNode(long pageId){
        if(nodes.containsKey(pageId)) return nodes.get(pageId);

        if(nodes.size() >= maxCapacity){
            Long oldId = pageOrder.poll();
            nodes.remove(oldId);
        }
        BPlusNode node = diskManager.readNode(pageId);

        nodes.put(node.pageId, node);
        pageOrder.add(node.pageId);

        return node;
    }

    public synchronized void saveNode(BPlusNode node){
        if (!nodes.containsKey(node.pageId)) {
            if (nodes.size() >= maxCapacity) {
                Long oldId = pageOrder.poll();
                nodes.remove(oldId);
            }
            nodes.put(node.pageId, node);
            pageOrder.add(node.pageId);
        }

        diskManager.saveNode(node);
    }

    public long allocatePage(){
        return diskManager.allocatePage();
    }

    public void updateRoot(long newRootId){
        diskManager.updateRoot(newRootId);
    }

    public long getRootPageId(){
        return diskManager.rootPageId;
    }
}
