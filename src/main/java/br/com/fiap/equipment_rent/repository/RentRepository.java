package br.com.fiap.equipment_rent.repository;

import br.com.fiap.equipment_rent.entity.Equipment;
import br.com.fiap.equipment_rent.entity.Rent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RentRepository extends JpaRepository<Rent, Long> {
    List<Rent> findByClassroomAndDate(int classroom, LocalDate date);
    //Find a rent with a specific rent and date
    List<Rent> findByEquipmentsContainingAndDate(Equipment equipment, LocalDate date);
}
