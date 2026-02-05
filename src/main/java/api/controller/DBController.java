package api.controller;

import api.dto.InsertRequest;
import api.service.DBService;
import btree.BPlusTree;
import models.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class DBController {

    private final DBService service;

    @Autowired
    public DBController(DBService service) { // <--- Inject Service
        this.service = service;
    }

    @PostMapping("/insert")
    public ResponseEntity<Map<String, Object>> insert(@RequestBody InsertRequest request){
        Map<String, Object> response = new HashMap<>();
        try{
            service.insertRecord(request.id(), request.name(), request.age());

            response.put("status", "success");
            response.put("message", "Record inserted");
            response.put("id", request.id());

            return ResponseEntity.ok(response);
        } catch (IllegalStateException e){
            response.put("status", "error");
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (IllegalArgumentException e){
            response.put("error", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/select/{id}")
    public ResponseEntity<Map<String, Object>> select(@PathVariable long id){
        Map<String, Object> response = new HashMap<>();
        try{
            Row result = service.selectRecord(id);

            response.put("status", "success");
            response.put("id", result.id());
            response.put("name", result.name());
            response.put("age", result.age());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e){
            response.put("status", "error");
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
