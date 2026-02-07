import btree.BPlusTree;
import cli.AngelShell;
import storage.BufferPool;
import storage.DiskManager;

public class Main {
    public static void main(String[] args) {
        DiskManager diskManager = new DiskManager("angel.db");
        BufferPool bufferPool = new BufferPool(50, diskManager);
        BPlusTree tree = new BPlusTree(39, bufferPool);
        AngelShell cli = new AngelShell(tree);
        
        cli.run();
    }
}