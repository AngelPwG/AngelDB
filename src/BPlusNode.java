import java.util.ArrayList;
import java.util.List;

public abstract class BPlusNode {
    int m;
    List<Long> keys;
    boolean isLeaf;
}
