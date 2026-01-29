import java.util.ArrayList;
import java.util.List;

public class LeafNode extends BPlusNode {
    List<Row> data = new ArrayList<Row>();
    LeafNode next;

    public LeafNode(int m, boolean isLeaf) {
        this.m = m;
        this.keys = new ArrayList<>();
        this.data = new ArrayList<>();
        this.next = null;
        this.isLeaf = isLeaf;
    }
}
