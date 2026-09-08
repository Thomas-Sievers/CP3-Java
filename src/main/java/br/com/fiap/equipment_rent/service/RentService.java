package br.com.fiap.equipment_rent.service;

import br.com.fiap.equipment_rent.entity.Equipment;
import br.com.fiap.equipment_rent.entity.Rent;
import br.com.fiap.equipment_rent.repository.RentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RentService {
    private final RentRepository repository;

    public RentService(RentRepository repository){
        this.repository = repository;
    }

    //Main rule: reservation must be made at least 7 days in advance
    private boolean checkDate(Rent rent){
        LocalDate today = LocalDate.now();
        LocalDate minimumAllowedDate = today.plusWeeks(1);

        return rent.getDate().isBefore(minimumAllowedDate);
    }

    //Business Rule 1 - Checks if any reserved equipment is already booked for an overlapping period
    private boolean checkEquipmentAvailability(Rent rent, Long excludeId){
        for (Equipment equipment : rent.getEquipments()) {
            List<Rent> conflicting = repository.findByEquipmentsContainingAndDate(equipment, rent.getDate());

            for (Rent other : conflicting) {
                if (other.getId().equals(excludeId)) {
                    continue;
                }

                //If the hour that the rent starts is before the hour
                boolean overlaps = rent.getTakenTime().isBefore(other.getReturnTime())
                        && other.getTakenTime().isBefore(rent.getReturnTime());

                if (overlaps) {
                    return true;
                }
            }
        }

        return false;
    }

    //Business Rule 2 & 3 - Check if there is another rent for the same classroom overlapping this time
    private boolean checkClassroom(Rent rent, Long excludeId){
        List<Rent> sameClassroom = repository.findByClassroomAndDate(rent.getClassroom(), rent.getDate());

        for (Rent other : sameClassroom) {
            if (other.getId().equals(excludeId)) {
                continue;
            }

            boolean overlaps = rent.getTakenTime().isBefore(other.getReturnTime())
                    && other.getTakenTime().isBefore(rent.getReturnTime());

            if (overlaps) {
                return true;
            }
        }

        return false;
    }

    //Business Rule 4 - Check if rent takenTime is before return time
    private boolean checkTime(Rent rent){
        return !rent.getTakenTime().isBefore(rent.getReturnTime());
    }

    //Business Rule 5 - Checks if every reserved equipment is active
    private boolean checkEquipmentActive(Rent rent){
        //Go through every equipment on rent and check the rule
        return rent.getEquipments().stream().anyMatch(equipment -> !equipment.getActive());
    }

    //Runs every business rule, each with its own rejection message
    private void validate(Rent rent, Long excludeId){
        if (checkDate(rent)) {
            throw new RuntimeException("The reservation must be made at least 7 days in advance");
        }
        if (checkTime(rent)) {
            throw new RuntimeException("The taken time must be before the return time");
        }
        if (checkEquipmentActive(rent)) {
            throw new RuntimeException("One or more selected equipment items are inactive");
        }
        if (checkClassroom(rent, excludeId)) {
            throw new RuntimeException("The classroom is already reserved for this time period");
        }
        if (checkEquipmentAvailability(rent, excludeId)) {
            throw new RuntimeException("One or more selected equipment items are already reserved for this time period");
        }
    }

    public List<Rent> list(){
        return repository.findAll();
    }

    public Rent searchById(Long id){
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Rent not found"));
    }

    public Rent save(Rent rent){
        validate(rent, null);
        return repository.save(rent);
    }

    public Rent update(Long id, Rent rent){
        validate(rent, id);

        //Check if the rent object exists
        Rent exists = searchById(id);

        exists.setTeacher(rent.getTeacher());
        exists.setCourse(rent.getCourse());
        exists.setClassroom(rent.getClassroom());
        exists.setDate(rent.getDate());
        exists.setTakenTime(rent.getTakenTime());
        exists.setReturnTime(rent.getReturnTime());
        exists.setEquipments(rent.getEquipments());

        return repository.save(exists);
    }

    public void delete(Long id){
        Rent rent = searchById(id);

        repository.delete(rent);
    }
}
