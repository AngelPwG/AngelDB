public class BPlusTree {
    public BPlusNode root;
    public int m;

    public BPlusTree(int m){
        this.root = null;
        this.m = m;
    }

    public Row search(long key) {
        if(root == null) return null;

        return recursiveSearch(root, key);
    }

    public Row recursiveSearch(BPlusNode node, long key){
        if(node.isLeaf){
            LeafNode requestedNode = (LeafNode) node;
            for (int i = 0; i < node.keys.size(); i++) {
                if (requestedNode.keys.get(i) == key) {
                    return requestedNode.data.get(i);
                }
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
}
