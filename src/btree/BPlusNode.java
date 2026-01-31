package btree;

import java.util.List;

public abstract class BPlusNode {
    public List<Long> keys;
    int t;
    boolean isLeaf;
}
