package br.com.fiap.equipment_rent.service;

import br.com.fiap.equipment_rent.entity.Rent;
import br.com.fiap.equipment_rent.repository.RentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RentService {
    private final RentRepository repository;

    public RentService(RentRepository repository){
        this.repository = repository;
    }

    public List<Rent> list(){
        return repository.findAll();
    }

    public Rent searchById(Long id){
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Rent not found"));
    }

    public Rent save(Rent rent){
        return repository.save(rent);
    }

    public Rent update(Long id, Rent rent){
        //Check if the rent object exists
        Rent exists = searchById(id);

        exists.setTeacher(rent.getTeacher());
        exists.setCourse(rent.getCourse());
        exists.setClassroom(rent.getClassroom());
        exists.setDate(rent.getDate());
        exists.setTakenTime(rent.getTakenTime());
        exists.setReturnTime(rent.getReturnTime());
        exists.setEquipment(rent.getEquipment());

        return repository.save(exists);
    }

    public void delete(Long id){
        Rent rent = searchById(id);

        repository.delete(rent);
    }
}
