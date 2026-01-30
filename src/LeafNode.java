import java.util.ArrayList;
import java.util.List;

public class LeafNode extends BPlusNode {
    List<Row> data;
    LeafNode next;

    public LeafNode(int m) {
        this.m = m;
        this.keys = new ArrayList<>();
        this.data = new ArrayList<>();
        this.next = null;
        this.isLeaf = true;
    }
}
