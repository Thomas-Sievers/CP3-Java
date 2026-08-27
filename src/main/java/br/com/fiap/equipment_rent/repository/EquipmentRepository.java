package br.com.fiap.equipment_rent.repository;

import br.com.fiap.equipment_rent.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

//Repository talks with DB

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    /*
    * JPA will recognize this method automatically
    *  findBy -> search
    *  Active -> attribute of Equipment
    *  True -> Only equipments that have Active = true
    *
    * SELECT *
    * FROM equipments
    * WHERE active = true
    * */

    List<Equipment> findByActiveTrue();
    List<Equipment> findByNameContainingIgnoreCase(String name); //Just use the same logic
}
