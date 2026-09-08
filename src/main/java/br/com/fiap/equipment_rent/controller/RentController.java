package br.com.fiap.equipment_rent.controller;

import br.com.fiap.equipment_rent.entity.Rent;
import br.com.fiap.equipment_rent.service.RentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rent")
public class RentController {

    private final RentService service;

    public RentController(RentService service) {
        this.service = service;
    }

    //GET http://localhost:8080/rent
    @GetMapping
    public List<Rent> list(){
        return service.list();
    }

    //GET http://localhost:8080/rent/{id}
    @GetMapping("/{id}")
    //? means that it can have two different bodies: Rent when is successful and a String when an error is raised
    public ResponseEntity<?> search(@PathVariable Long id){
        try {
            return ResponseEntity.ok(service.searchById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //POST http://localhost:8080/rent
    @PostMapping
    public ResponseEntity<?> post(@RequestBody Rent rent){
        try {
            return ResponseEntity.ok(service.save(rent));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //PUT http://localhost:8080/rent/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Rent rent){
        try {
            return ResponseEntity.ok(service.update(id, rent));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //DELETE http://localhost:8080/rent/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
