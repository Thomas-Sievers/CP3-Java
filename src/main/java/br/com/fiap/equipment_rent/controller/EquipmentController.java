package br.com.fiap.equipment_rent.controller;

import br.com.fiap.equipment_rent.entity.Equipment;
import br.com.fiap.equipment_rent.service.EquipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//Controller is the one who handle HTTP requests

//Define the address using endpoints of this class
@RestController

//GET http://localhost:8080/equipment
@RequestMapping("/equipment")
public class EquipmentController {

    //Controller delegates business rules to service
    private final EquipmentService service;

    //Constructor utilized by spring to inject dependencies
    public EquipmentController(EquipmentService service) {
        this.service = service;
    }

    /*
    * @GetMapping indicates that this method is called when app receive any HTTP GET request
    *
    * Spring transforms the value of return into JSON
    * */
    @GetMapping
    public List<Equipment> list(){
        return service.list();
    }

    //GET http://localhost:8080/equipement/{id}
    @GetMapping("/{id}")
    //? means that it can have two different bodies: Equipment when is successful and a String when an error is raised
    public ResponseEntity<?> search(@PathVariable Long id){ //@PathVariable gets the value of {id} and add to path
        try {
            return ResponseEntity.ok( //Response of HTTP (ok == 200)
                    service.searchById(id)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //POST http://localhost:8080/equipment
    @PostMapping
    public ResponseEntity<?> post(@RequestBody Equipment equipment){ //@Requestbody transform the JSON sent by the user into an object
        try {
            return ResponseEntity.ok(
                    service.save(equipment)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //PUT http://localhost/equipment/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Equipment equipment){
        try {
            return ResponseEntity.ok(
                    service.update(id, equipment)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //DELETE http://localhost/equipment/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        try {
            service.delete(id);

            return ResponseEntity.noContent().build(); //noContent() returns 204 == operation successful but no return
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
