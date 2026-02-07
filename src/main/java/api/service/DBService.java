package api.service;

import btree.BPlusTree;
import models.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DBService {
    private final BPlusTree engine;

    @Autowired
    public DBService(BPlusTree engine){
        this.engine = engine;
    }

    public void insertRecord(long id, String name, int age){
        if(age < 0 || age > 110)
            throw new IllegalArgumentException("Age cannot be negative or above 110 years.");

        if(name == null || name.isEmpty())
            throw new IllegalArgumentException("Name cannot be empty.");

        Row record = new Row(id, name, age);
        if(!engine.insert(id, record))
            throw new IllegalStateException("Record with ID " + id + " already exists.");
    }

    public Row selectRecord(long id) {
        Row result = engine.search(id);

        if (result == null)
            throw new RuntimeException("Record with ID " + id + " not found.");

        return result;
    }

    public List<Row> selectAll(){
        List<Row> result = engine.selectAll();

        if(result.isEmpty())
            throw new RuntimeException("There are no records.");

        return result;
    }

    public List<Row> selectRange(long start, long end){
        if(start > end)
            throw new IllegalArgumentException("Start ID cannot be greater than End ID.");

        List<Row> result = engine.selectBetween(start, end);

        if(result.isEmpty())
            throw new RuntimeException("There are no records between ID " + start + " and " + end + ".");

        return result;
    }

    public void update(long id, String name, int age){
        if(age < 0 || age > 110)
            throw new IllegalArgumentException("Age cannot be negative or above 110 years");

        if(name == null || name.isEmpty())
            throw new IllegalArgumentException("Name cannot be empty");

        Row record = new Row(id, name, age);

        if(!engine.update(record))
            throw new RuntimeException("Record with ID " + id + " not found.");
    }

    public void delete(long id){
        if(!engine.delete(id))
            throw new RuntimeException("Record with ID " + id + " not found.");
    }
}
