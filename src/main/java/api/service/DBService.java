package api.service;

import btree.BPlusTree;
import models.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DBService {
    private final BPlusTree engine;

    @Autowired
    public DBService(BPlusTree engine){
        this.engine = engine;
    }

    public void insertRecord(long id, String name, int age){
        if(age < 0 || age > 120)
            throw new IllegalArgumentException("Age cannot be negative or above 120 years");
        if(name == null || name.isEmpty())
            throw new IllegalArgumentException("Name cannot be empty");
        Row record = new Row(id, name, age);
        if(!engine.insert(id, record))
            throw new IllegalStateException("Record with ID " + id + " already exists.");
    }

    public Row selectRecord(long id) {
        Row result = engine.search(id);

        if (result == null) {
            throw new RuntimeException("Record with ID " + id + " not found.");
        }

        return result;
    }
}
