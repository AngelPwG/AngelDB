import java.util.ArrayList;
import java.util.List;

public class InternalNode extends BPlusNode {
    List<BPlusNode> children;

    public InternalNode(int m, boolean isLeaf) {
        this.m = m;
        this.keys = new ArrayList<>();
        this.isLeaf = isLeaf;
        this.children = new ArrayList<>();
    }
}
