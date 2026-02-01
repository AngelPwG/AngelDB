package btree;

import models.Row;
import storage.DiskManager;

import java.io.File;
import java.util.RandomAccess;

public class BPlusTree {
    public BPlusNode root;
    public int t;
    private DiskManager diskManager;

    public BPlusTree(int t){
        this.root = null;
        this.t = t;
        diskManager = new DiskManager("angeldb");
    }

    public Row search(long key) {
        if(root == null) return null;

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



        return recursiveSearch(internalNode.children.get(i), key);
    }

    public boolean insert(long key, Row record){
        BPlusNode r = root;

        if(r == null){
            LeafNode root = new LeafNode(t);
            root.keys.add(key);
            root.data.add(record);
            this.root = root;
            return true;
        }

        if(r.keys.size() == 2 * t - 1){
            InternalNode newRoot = new InternalNode(t);
            root = newRoot;
            newRoot.children.add(r);

            splitChild(newRoot, 0, r);

            return insertNonFull(newRoot, key, record);
        }

        return insertNonFull(r, key, record);
    }

    private void splitChild(InternalNode parent, int i, BPlusNode fullChild){
        if(fullChild.isLeaf){
            LeafNode oldChild = (LeafNode) fullChild;
            LeafNode newChild = new LeafNode(t);

            newChild.next = oldChild.next;
            oldChild.next = newChild;

            newChild.keys.addAll(oldChild.keys.subList(t - 1, oldChild.keys.size()));
            newChild.data.addAll(oldChild.data.subList(t - 1, oldChild.data.size()));
            oldChild.keys.subList(t - 1, oldChild.keys.size()).clear();
            oldChild.data.subList(t - 1, oldChild.data.size()).clear();

            parent.children.add(i + 1, newChild);
            parent.keys.add(i, newChild.keys.getFirst());
        }else{
            InternalNode newChild = new InternalNode(t);
            InternalNode oldChild = (InternalNode) fullChild;

            newChild.keys.addAll(oldChild.keys.subList(t - 1, oldChild.keys.size()));
            oldChild.keys.subList(t - 1, oldChild.keys.size()).clear();
            newChild.children.addAll(oldChild.children.subList(t, oldChild.children.size()));
            oldChild.children.subList(t, oldChild.children.size()).clear();

            parent.keys.add(i, newChild.keys.getFirst());
            parent.children.add(i + 1, newChild);
            newChild.keys.removeFirst();
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

            return true;
        }else{
            InternalNode internal = (InternalNode) node;

            if(internal.children.get(i).keys.size() == 2 * t - 1){
                splitChild(internal, i, internal.children.get(i));

                if(key >= internal.keys.get(i))
                    i++;
            }

            return insertNonFull(internal.children.get(i), key, record);
        }
    }

    public void printStructure(){
        printRecursively(root, 0);
    }

    private void printRecursively(BPlusNode node, int level){
        if(node == null) return;

        System.out.println(" ".repeat(level) + "Level " + level + ": " + node.keys);

        if(!node.isLeaf){
            for(BPlusNode child : ((InternalNode)node).children){
                printRecursively(child, level + 1);
            }
        }
    }
}
