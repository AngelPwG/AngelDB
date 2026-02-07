package api.controller;

import api.dto.InsertRequest;
import api.service.DBService;
import models.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class DBController {

    private final DBService service;

    @Autowired
    public DBController(DBService service) {
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
            response.put("data", result);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e){
            response.put("status", "error");
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/selectAll")
    public ResponseEntity<Map<String, Object>> selectAll(){
        Map<String, Object> response = new HashMap<>();
        try{
            List<Row> result = service.selectAll();

            response.put("status", "success");
            response.put("total-records", result.size());
            response.put("data", result);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e){
            response.put("status", "error");
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateRecord(@RequestBody InsertRequest request){
        Map<String, Object> response = new HashMap<>();
        try{
            service.update(request.id(), request.name(), request.age());

            response.put("status", "success");
            response.put("message", "Record updated");
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

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteRecord(@PathVariable long id){
        Map<String, Object> response = new HashMap<>();
        try{
            service.delete(id);

            response.put("status", "success");
            response.put("message", "Record deleted");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e){
            response.put("status", "error");
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/selectRange")
    public ResponseEntity<Map<String, Object>> selectRange(@RequestParam long start, @RequestParam long end){
        Map<String, Object> response = new HashMap<>();
        try {
            List<Row> result = service.selectRange(start, end);

            response.put("status", "success");
            response.put("total-records", result.size());
            response.put("data", result);

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
