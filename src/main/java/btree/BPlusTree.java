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

    private InternalNode getParent(InternalNode node, long childPageId, long key){
        if(node.childrenIDs.contains(childPageId))
            return node;

        int i = 0;

        while(i < node.keys.size() && key >= node.keys.get(i)){
            i++;
        }

        InternalNode child = (InternalNode) bufferPool.getNode(node.childrenIDs.get(i));

        return getParent(child, childPageId, key);
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

    public boolean delete(long id){
        LeafNode node = findLeaf(root, id);
        boolean found = false;
        for(int i = 0; i < node.keys.size(); i++){
            if(node.keys.get(i) == id){
                node.keys.remove(i);
                node.data.remove(i);
                found = true;
                break;
            }
        }

        if(!found) return false;
        if(node.pageId == root.pageId || node.keys.size() >= t - 1){
            bufferPool.saveNode(node);
            return true;
        }
        System.out.println("UNDERFLOW DETECTED on Page " + node.pageId);
        return handleUnderflow(node, id);
    }

    private boolean handleUnderflow(BPlusNode node, long id){
        InternalNode parent = getParent((InternalNode) root, node.pageId, id);
        BPlusNode rightSibling = null;
        BPlusNode leftSibling = null;

        int index = -1;

        for(int i = 0; i < parent.childrenIDs.size(); i++){
            if(parent.childrenIDs.get(i) == node.pageId){
                index = i;
                break;
            }
        }

        if(index > 0)
            leftSibling = bufferPool.getNode(parent.childrenIDs.get(index - 1));
        else if(index < parent.childrenIDs.size() - 1)
            rightSibling = bufferPool.getNode(parent.childrenIDs.get(index + 1));
        return borrowOrMerge(node, leftSibling, rightSibling, parent, index);
    }

    private boolean borrowOrMerge(BPlusNode node, BPlusNode left, BPlusNode right, InternalNode parent, int index){
        if(left != null && left.keys.size() > t - 1){
            if(node.isLeaf)
                return borrowFromLeft((LeafNode) node, (LeafNode) left, parent, index);
            else
                return borrowFromLeftInternal((InternalNode) node, (InternalNode) left, parent, index);
        }
        if(right != null && right.keys.size() > t - 1){
            if(node.isLeaf)
                return borrowFromRight((LeafNode) node, (LeafNode) right, parent, index);
            else
                return borrowFromRightInternal((InternalNode) node, (InternalNode) right, parent, index);
        }

        if (left != null) {
            return mergeWithLeft(node, left, parent, index);
        } else if (right != null) {
            return mergeWithLeft(right, node, parent, index + 1);
        }
        return false;
    }

    private boolean borrowFromLeft(LeafNode node, LeafNode left, InternalNode parent, int index){
        node.keys.addFirst(left.keys.getLast());
        left.keys.removeLast();
        node.data.addFirst(left.data.getLast());
        left.data.removeLast();

        parent.keys.set(index - 1, node.keys.getFirst());

        bufferPool.saveNode(node);
        bufferPool.saveNode(parent);
        bufferPool.saveNode(left);
        return true;
    }

    private boolean borrowFromRight(LeafNode node, LeafNode right, InternalNode parent, int index){
        node.keys.addLast(right.keys.getFirst());
        right.keys.removeFirst();
        node.data.addLast(right.data.getFirst());
        right.data.removeFirst();

        parent.keys.set(index, right.keys.getFirst());

        bufferPool.saveNode(node);
        bufferPool.saveNode(parent);
        bufferPool.saveNode(right);
        return true;
    }

    private boolean borrowFromLeftInternal(InternalNode node, InternalNode left, InternalNode parent, int index){
        node.keys.addFirst(parent.keys.get(index - 1));
        parent.keys.set(index - 1, left.keys.getLast());
        left.keys.removeLast();
        node.childrenIDs.addFirst(left.childrenIDs.getLast());
        left.childrenIDs.removeLast();

        bufferPool.saveNode(node);
        bufferPool.saveNode(parent);
        bufferPool.saveNode(left);
        return true;
    }

    private boolean borrowFromRightInternal(InternalNode node, InternalNode right, InternalNode parent, int index){
        node.keys.addLast(parent.keys.get(index));
        parent.keys.set(index, right.keys.getFirst());
        right.keys.removeFirst();
        node.childrenIDs.addLast(right.childrenIDs.getFirst());
        right.childrenIDs.removeFirst();

        bufferPool.saveNode(node);
        bufferPool.saveNode(parent);
        bufferPool.saveNode(right);
        return true;
    }

    private boolean mergeWithLeft(BPlusNode node, BPlusNode left, InternalNode parent, int index) {
        if(node.isLeaf){
            left.keys.addAll(node.keys);
            ((LeafNode)left).data.addAll(((LeafNode)node).data);

            ((LeafNode)left).nextPointer = ((LeafNode)node).nextPointer;
        }else{
            left.keys.add(parent.keys.get(index - 1));
            left.keys.addAll(node.keys);

            ((InternalNode)left).childrenIDs.addAll(((InternalNode)node).childrenIDs);
        }
        parent.keys.remove(index - 1);
        parent.childrenIDs.remove(index);

        bufferPool.saveNode(parent);
        bufferPool.saveNode(left);

        if(parent.pageId == root.pageId && parent.keys.isEmpty())
            bufferPool.updateRoot(left.pageId);
        else if(parent.pageId != root.pageId && parent.keys.size() < t - 1)
            return handleUnderflow(parent, parent.keys.getFirst());
        return true;
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
