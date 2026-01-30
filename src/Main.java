public class Main {
    public static void main(String[] args) {
        BPlusTree tree = new BPlusTree(3);

        InternalNode root = new InternalNode(3);
        LeafNode leftLeaf = new LeafNode(3);
        LeafNode rightLeaf = new LeafNode(3);

        leftLeaf.next = rightLeaf;

        root.keys.add(30L);
        root.children.add(leftLeaf);
        root.children.add(rightLeaf);

        leftLeaf.keys.add(10L);
        leftLeaf.data.add(new Row(10L, "Jose", 20));
        leftLeaf.keys.add(20L);
        leftLeaf.data.add(new Row(20L, "Maria", 22));

        rightLeaf.keys.add(30L);
        rightLeaf.data.add(new Row(30L, "Pedro", 25));
        rightLeaf.keys.add(40L);
        rightLeaf.data.add(new Row(40L, "Juan", 35));

        tree.root = root;

        System.out.println("Searching for 20: " + tree.search(20));
        System.out.println("Searching for 30: " + tree.search(30));
    }
}