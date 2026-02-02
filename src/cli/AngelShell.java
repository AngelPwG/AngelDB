package cli;

import btree.BPlusTree;
import models.Row;

import java.util.List;
import java.util.Scanner;

public class AngelShell {
    private final BPlusTree tree;
    private final Scanner scanner;

    public AngelShell(BPlusTree tree){
        this.tree = tree;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println("--- ANGEL DATABASE ENGINE (v1.0) ---");
        System.out.println("Type 'help' for commands or 'exit' to quit");

        while (true){
            System.out.print("angel> ");
            String input = scanner.nextLine().trim();

            if(input.equalsIgnoreCase("exit")) break;

            handleCommand(input);
        }

        System.out.println("Shutting down...");
    }

    public void handleCommand(String input){
        String[] parts = input.split(" ");
        String command = parts[0].toLowerCase();

        switch (command){
            case "insert":
                handleInsert(parts);
                break;
            case "select":
                handleSelect(parts);
                break;
            case "update":
                handleUpdate(parts);
                break;
            case "delete":
                handleDelete(parts);
                break;
            case "help":
                printHelp();
                break;
            case "bulk_insert":
                bulkInsert(parts);
                break;
            case "bulk_delete":
                bulkDelete(parts);
                break;
            default:
                System.out.println("Unknown command. Type 'help' for usage.");
        }
    }

    private void handleInsert(String[] parts){
        try{
            long id = Long.parseLong(parts[1]);
            Row record = parseRow(parts);
            if(tree.insert(id, record))
                System.out.println("Query OK, 1 row affected");
            else
                System.out.println("Error: Duplicate ID " + id);
        }catch (NumberFormatException | ArrayIndexOutOfBoundsException e){
            System.out.println("Syntax Error: insert <id> <name> <age>");
        }
    }

    private void handleSelect(String[] parts){
        try {
            if (parts.length == 4 && parts[1].equalsIgnoreCase("range")){
                System.out.println("+------+----------------------------------+-----+");
                System.out.println("| ID   | Name                             | Age |");
                System.out.println("+------+----------------------------------+-----+");
                long start = Long.parseLong(parts[2]);
                long end = Long.parseLong(parts[3]);
                if (start > end) System.out.println("Error: start should be lower than end.");
                else{
                    List<Row> results = tree.selectBetween(start, end);
                    for (Row row : results) {
                        System.out.printf("| %-4d | %-32s | %-3d |\n", row.id(), row.name(), row.age());
                    }
                    System.out.println("+------+----------------------------------+-----+");
                    System.out.println(results.size() + " rows in set.");
                }
            }else if (parts.length == 2 && parts[1].equals("*")) {
                System.out.println("+------+----------------------------------+-----+");
                System.out.println("| ID   | Name                             | Age |");
                System.out.println("+------+----------------------------------+-----+");
                List<Row> results = tree.selectAll();

                for (Row row : results) {
                    System.out.printf("| %-4d | %-32s | %-3d |\n", row.id(), row.name(), row.age());
                }
                System.out.println("+------+----------------------------------+-----+");
                System.out.println(results.size() + " rows in set.");
            } else if(parts.length == 2) {
                System.out.println("+------+----------------------------------+-----+");
                System.out.println("| ID   | Name                             | Age |");
                System.out.println("+------+----------------------------------+-----+");
                long id = Long.parseLong(parts[1]);
                Row result = tree.search(id);
                if (result != null) {
                    System.out.printf("| %-4d | %-32s | %-3d |\n", result.id(), result.name(), result.age());
                    System.out.println("+------+----------------------------------+-----+");
                }
                else
                    System.out.println("No record found with ID " + id);
            } else {
                System.out.println("Syntax Error: select <id> || select * || select range <start> <end>");
            }
        }catch (NumberFormatException | ArrayIndexOutOfBoundsException e){
            System.out.println("Syntax Error: select <id> || select * || select range <start> <end>");
        }
    }

    private void handleUpdate(String[] parts){
        try{
            long id = Long.parseLong(parts[1]);
            Row record = parseRow(parts);
            if(tree.update(record))
                System.out.println("Query OK, 1 row affected (Updated ID " + id + ")");
            else
                System.out.println("Error: There is no record with ID " + id);
        }catch (NumberFormatException | ArrayIndexOutOfBoundsException e){
            System.out.println("Syntax Error: update <id> <name> <age>");
        }
    }

    private void handleDelete(String[] parts){
        try{
            if(parts.length > 2){
                System.out.println("Syntax Error: delete <id>");
                return;
            }

            long id = Long.parseLong(parts[1]);
            if(tree.delete(id)){
                System.out.println("Query OK, 1 row affected (Deleted ID " + id + ")");
            }else{
                System.out.println("Error: There is no record with ID " + id);
            }
        }catch (NumberFormatException | ArrayIndexOutOfBoundsException e){
            System.out.println("Syntax Error: delete <id>");
        }
    }

    private Row parseRow(String[] parts){
        long id = Long.parseLong(parts[1]);
        StringBuilder name = new StringBuilder();
        for(int i = 2; i < parts.length - 1; i ++){
            name.append(parts[i]);

            if (i < parts.length - 2) {
                name.append(" ");
            }
        }
        int age = Integer.parseInt(parts[parts.length - 1]);
        return new Row(
                id,
                name.toString(),
                age
        );
    }

    private void printHelp() {
        System.out.println("Available Commands:");
        System.out.println("  insert <id> <name> <age>");
        System.out.println("  select <id>");
        System.out.println("  select *");
        System.out.println("  delete <id>");

        System.out.println("  exit");
    }

    private void bulkInsert(String[] parts){
        // Syntax: bulk_insert <count>
        try{
            int count = Integer.parseInt(parts[1]);
            System.out.println("Starting bulk insert of " + count + " records...");

            long start = System.currentTimeMillis();

            for (int i = 1; i <= count; i++) {
                tree.insert(i, new Row(i, "User " + i, 20 + (i % 50)));

                if (i % 100 == 0) {
                    System.out.println("... inserted " + i + " records");
                }
            }

            long end = System.currentTimeMillis();
            System.out.println("Success! Inserted " + count + " records in " + (end - start) + "ms.");
        } catch (Exception e) {
            System.out.println("Syntax: bulk_insert <count>");
        }
    }

    private void bulkDelete(String[] parts){
        // Syntax: bulk_delete <start_id> <count>
        try {
            long startId = Long.parseLong(parts[1]);
            int amount = Integer.parseInt(parts[2]);

            System.out.println("Starting BULK DELETE of " + amount + " records starting from ID " + startId + "...");
            long startTime = System.currentTimeMillis();

            for (long i = startId; i < startId + amount; i++) {
                boolean success = tree.delete(i);
                if (!success) {
                    System.out.println("Failed to delete ID: " + i + " (Maybe it doesn't exist?)");
                }

                if (i % 50 == 0) System.out.println("... deleted up to ID " + i);
            }
            long endTime = System.currentTimeMillis();
            System.out.println("Bulk delete finished in " + (endTime - startTime) + "ms.");
        } catch (Exception e) {
            System.out.println("Syntax: bulk_delete <start_id> <amount>");
        }
    }
}
