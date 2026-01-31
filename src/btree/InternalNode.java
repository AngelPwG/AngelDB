package btree;

import java.util.ArrayList;
import java.util.List;

public class InternalNode extends BPlusNode {
    public List<BPlusNode> children;

    public InternalNode(int t) {
        this.t = t;
        this.keys = new ArrayList<>();
        this.isLeaf = false;
        this.children = new ArrayList<>();
    }
}
