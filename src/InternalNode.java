import java.util.ArrayList;
import java.util.List;

public class InternalNode extends BPlusNode {
    List<BPlusNode> children;

    public InternalNode(int m) {
        this.m = m;
        this.keys = new ArrayList<>();
        this.isLeaf = false;
        this.children = new ArrayList<>();
    }
}
