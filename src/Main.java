import btree.BPlusTree;
import cli.AngelShell;
import storage.DiskManager;

public class Main {
    public static void main(String[] args) {
        AngelShell cli = new AngelShell(new BPlusTree(39, new DiskManager("angel.db")));
        cli.run();
    }
}