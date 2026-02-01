package btree;

import models.Row;
import storage.BufferPool;

import java.util.ArrayList;
import java.util.List;

public class BPlusTree {
    public BPlusNode root;
    public int t;
    private BufferPool bufferPool;

    public BPlusTree(int t, BufferPool bufferPool){
        this.t = t;
        this.bufferPool = bufferPool;
        if(bufferPool.getRootPageId() > 0){
            this.root = bufferPool.getNode(bufferPool.getRootPageId());
        }else{
            this.root = null;
        }
    }

    public Row search(long key) {
        return recursiveSearch(root, key);
    }

    private Row recursiveSearch(BPlusNode node, long key){
        if(node.isLeaf){
            LeafNode requestedNode = (LeafNode) node;
            for (int i = 0; i < node.keys.size(); i++) {
                if(requestedNode.keys.get(i) == key) return requestedNode.data.get(i);
            }
            return null;
        }
        int i = 0;

        while(i < node.keys.size() && key >= node.keys.get(i)){
            i++;
        }

        InternalNode internalNode = (InternalNode) node;
        long pageId = internalNode.childrenIDs.get(i);

        return recursiveSearch(bufferPool.getNode(pageId), key);
    }

    private LeafNode findLeaf(BPlusNode node, long key){
        if(node.isLeaf)
            return (LeafNode) node;

        int i = 0;

        while(i < node.keys.size() && key >= node.keys.get(i)){
            i++;
        }

        InternalNode internalNode = (InternalNode) node;
        long pageId = internalNode.childrenIDs.get(i);

        return findLeaf(bufferPool.getNode(pageId), key);
    }

    public boolean insert(long key, Row record){
        BPlusNode r = root;

        if(r == null){
            LeafNode root = new LeafNode(t);
            root.pageId = bufferPool.allocatePage();

            root.keys.add(key);
            root.data.add(record);
            this.root = root;

            bufferPool.saveNode(this.root);
            bufferPool.updateRoot(root.pageId);
            return true;
        }

        if(r.keys.size() == 2 * t - 1){
            InternalNode newRoot = new InternalNode(t);
            newRoot.pageId = bufferPool.allocatePage();

            newRoot.childrenIDs.add(r.pageId);
            splitChild(newRoot, 0, r);

            root = newRoot;
            bufferPool.saveNode(root);
            bufferPool.updateRoot(root.pageId);
            return insertNonFull(newRoot, key, record);
        }

        return insertNonFull(r, key, record);
    }

    private void splitChild(InternalNode parent, int i, BPlusNode fullChild){
        if(fullChild.isLeaf){
            LeafNode oldChild = (LeafNode) fullChild;
            LeafNode newChild = new LeafNode(t);

            newChild.pageId = bufferPool.allocatePage();

            newChild.nextPointer = oldChild.nextPointer;
            oldChild.nextPointer = newChild.pageId;

            newChild.keys.addAll(oldChild.keys.subList(t - 1, oldChild.keys.size()));
            newChild.data.addAll(oldChild.data.subList(t - 1, oldChild.data.size()));
            oldChild.keys.subList(t - 1, oldChild.keys.size()).clear();
            oldChild.data.subList(t - 1, oldChild.data.size()).clear();

            parent.childrenIDs.add(i + 1, newChild.pageId);
            parent.keys.add(i, newChild.keys.getFirst());

            bufferPool.saveNode(newChild);
            bufferPool.saveNode(oldChild);
            bufferPool.saveNode(parent);
        }else{
            InternalNode newChild = new InternalNode(t);
            InternalNode oldChild = (InternalNode) fullChild;
            newChild.pageId = bufferPool.allocatePage();

            newChild.keys.addAll(oldChild.keys.subList(t - 1, oldChild.keys.size()));
            oldChild.keys.subList(t - 1, oldChild.keys.size()).clear();
            newChild.childrenIDs.addAll(oldChild.childrenIDs.subList(t, oldChild.childrenIDs.size()));
            oldChild.childrenIDs.subList(t, oldChild.childrenIDs.size()).clear();

            parent.keys.add(i, newChild.keys.getFirst());
            parent.childrenIDs.add(i + 1, newChild.pageId);
            newChild.keys.removeFirst();

            bufferPool.saveNode(newChild);
            bufferPool.saveNode(oldChild);
            bufferPool.saveNode(parent);
        }
    }

    private boolean insertNonFull(BPlusNode node, long key, Row record){
        int i = 0;

        while(i < node.keys.size() && key >= node.keys.get(i)){
            if(node.keys.get(i) == key) return false;
            i++;
        }

        if(node.isLeaf){
            node.keys.add(i, key);
            ((LeafNode)node).data.add(i, record);
            bufferPool.saveNode(node);

            return true;
        }else{
            InternalNode internal = (InternalNode) node;
            BPlusNode childNode = bufferPool.getNode(internal.childrenIDs.get(i));
            if(childNode.keys.size() == 2 * t - 1) {
                splitChild(internal, i, childNode);

                if (key >= internal.keys.get(i)) {
                    i++;
                    childNode = bufferPool.getNode(internal.childrenIDs.get(i));
                }
            }


            return insertNonFull(childNode, key, record);
        }
    }

    public void printStructure(){
        printRecursively(root, 0);
    }

    private void printRecursively(BPlusNode node, int level){
        if(node == null) return;

        System.out.println(" ".repeat(level) + "Level " + level + ": " + node.keys);

        if(!node.isLeaf){
            for(long pageId : ((InternalNode)node).childrenIDs){
                BPlusNode child = bufferPool.getNode(pageId);
                printRecursively(child, level + 1);
            }
        }else{
            LeafNode leaf = (LeafNode) node;
            System.out.println(" ".repeat(level) + " -> Next Leaf Page ID: " + leaf.nextPointer);
        }
    }

    public boolean update(Row record){
        LeafNode node = findLeaf(root, record.id());

        for(int i = 0; i < node.keys.size(); i++){
            if(node.keys.get(i) == record.id()){
                node.data.set(i, record);
                bufferPool.saveNode(node);
                return true;
            }
        }

        return false;
    }

    public List<Row> selectAll(){
        List<Row> records = new ArrayList<>();
        BPlusNode node = root;

        while (!node.isLeaf) {
            long firstChildID = ((InternalNode) node).childrenIDs.getFirst();
            node = bufferPool.getNode(firstChildID);
        }

        LeafNode leafNode = (LeafNode) node;

        while(true){
            records.addAll(leafNode.data);

            if(leafNode.nextPointer == 0) break;

            leafNode = (LeafNode) bufferPool.getNode(leafNode.nextPointer);
        }

        return records;
    }

    public List<Row> selectBetween(long startId, long endId){
        List<Row> results = new ArrayList<>();

        LeafNode leaf = findLeaf(root, startId);

        boolean stop = false;
        while (!stop) {
            for (int i = 0; i < leaf.keys.size(); i++) {
                long key = leaf.keys.get(i);

                if (key > endId) {
                    stop = true;
                    break;
                }

                if (key >= startId) {
                    results.add(leaf.data.get(i));
                }
            }

            if(!stop && leaf.nextPointer != 0)
                leaf = (LeafNode) bufferPool.getNode(leaf.nextPointer);
            else
                break;
        }

        return results;
    }
}
