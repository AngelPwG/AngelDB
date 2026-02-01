package btree;

import models.Row;

import java.util.ArrayList;
import java.util.List;

public class LeafNode extends BPlusNode {
    public List<Row> data;
    public Long nextPointer;

    public LeafNode(int t) {
        this.t = t;
        this.keys = new ArrayList<>();
        this.data = new ArrayList<>();
        this.nextPointer = null;
        this.isLeaf = true;
    }
}
