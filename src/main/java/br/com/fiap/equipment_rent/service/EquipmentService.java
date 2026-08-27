package br.com.fiap.equipment_rent.service;

import br.com.fiap.equipment_rent.entity.Equipment;
import br.com.fiap.equipment_rent.repository.EquipmentRepository;
import org.springframework.stereotype.Service;

//Service is the one responsible for checking business rules
@Service
public class EquipmentService {
    //Final means that this cannot be changed after the object has been created
    private final EquipmentRepository repository;

    //Spring automatically does dependencies injection
    public EquipmentService(EquipmentRepository repository){
        this.repository = repository;
    }

    public Equipment searchById(Long id){
        //Search by id - if the object isn't found raise an exception
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Equipment not found"));
    }

    public Equipment save(Equipment equipment){
        return repository.save(equipment);
    }

    public Equipment update(Long id, Equipment equipment){
        //First we need to verify if the product exists
        Equipment exists = searchById(id);

        //Updates all fields
        exists.setName(equipment.getName());
        exists.setDescription(equipment.getDescription());
        exists.setActive(equipment.getActive());
        exists.setStock(equipment.getStock());

        //Since this object already has an ID - JPA treats like a update
        return repository.save(exists);
    }

    public void delete(Long id){
        Equipment equipment = searchById(id);

        repository.delete(equipment);
    }
}
