package br.com.fiap.equipment_rent.repository;

import br.com.fiap.equipment_rent.entity.Rent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RentRepository extends JpaRepository<Rent, Long> {
    List<Rent> findByTeacherContainingIgnoreCase(String teacher);
    List<Rent> findByClassroom(int classroom);
}
