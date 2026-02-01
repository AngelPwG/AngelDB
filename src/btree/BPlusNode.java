package btree;

import java.util.List;

public abstract class BPlusNode {
    public List<Long> keys;
    public long pageId;
    int t;
    boolean isLeaf;
}
