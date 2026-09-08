package br.com.fiap.equipment_rent.service;

import br.com.fiap.equipment_rent.entity.Equipment;
import br.com.fiap.equipment_rent.entity.Rent;
import br.com.fiap.equipment_rent.repository.RentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentServiceTest {

    @Mock
    private RentRepository repository;

    private RentService service;

    @BeforeEach
    void setUp() {
        service = new RentService(repository);
    }

    private Equipment activeEquipment(Long id) {
        return Equipment.builder().id(id).name("Datashow").stock(1).active(true).build();
    }
    
    private Rent validRent() {
        return Rent.builder()
                .teacher("Joao da Silva")
                .course("Engenharia de Software")
                .classroom(204)
                .date(LocalDate.now().plusWeeks(2))
                .takenTime(LocalTime.of(18, 30))
                .returnTime(LocalTime.of(22, 30))
                .equipments(List.of(activeEquipment(1L)))
                .build();
    }

    //Main rule: at least 7 days in advance

    @Test
    void save_rejectsReservationLessThanSevenDaysAhead() {
        Rent rent = validRent();
        rent.setDate(LocalDate.now().plusDays(2));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.save(rent));
        assertEquals("The reservation must be made at least 7 days in advance", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void save_allowsReservationExactlySevenDaysAhead() {
        Rent rent = validRent();
        rent.setDate(LocalDate.now().plusWeeks(1));

        when(repository.findByClassroomAndDate(anyInt(), any())).thenReturn(List.of());
        when(repository.findByEquipmentsContainingAndDate(any(), any())).thenReturn(List.of());
        when(repository.save(rent)).thenReturn(rent);

        assertDoesNotThrow(() -> service.save(rent));
    }

    //Rule 4: takenTime must be before returnTime (and never equal)

    @Test
    void save_rejectsWhenTakenTimeIsAfterReturnTime() {
        Rent rent = validRent();
        rent.setTakenTime(LocalTime.of(22, 0));
        rent.setReturnTime(LocalTime.of(18, 0));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.save(rent));
        assertEquals("The taken time must be before the return time", ex.getMessage());
    }

    @Test
    void save_rejectsWhenTakenTimeEqualsReturnTime() {
        Rent rent = validRent();
        rent.setTakenTime(LocalTime.of(18, 0));
        rent.setReturnTime(LocalTime.of(18, 0));

        assertThrows(RuntimeException.class, () -> service.save(rent));
    }

    //Rule 5: every reserved equipment must be active

    @Test
    void save_rejectsWhenAnyReservedEquipmentIsInactive() {
        Rent rent = validRent();
        rent.setEquipments(List.of(Equipment.builder().id(2L).name("Microfone").stock(1).active(false).build()));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.save(rent));
        assertEquals("One or more selected equipment items are inactive", ex.getMessage());
    }

    //Rules 2 & 3: classroom cannot have overlapping reservations

    @Test
    void save_rejectsOverlappingClassroomReservation() {
        Rent rent = validRent();

        Rent existing = validRent();
        existing.setId(1L);
        existing.setTakenTime(LocalTime.of(19, 0));
        existing.setReturnTime(LocalTime.of(21, 0));

        when(repository.findByClassroomAndDate(rent.getClassroom(), rent.getDate())).thenReturn(List.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.save(rent));
        assertEquals("The classroom is already reserved for this time period", ex.getMessage());
    }

    @Test
    void save_allowsBackToBackClassroomReservations() {
        Rent rent = validRent();
        rent.setTakenTime(LocalTime.of(20, 0));
        rent.setReturnTime(LocalTime.of(22, 0));

        Rent existing = validRent();
        existing.setId(1L);
        existing.setTakenTime(LocalTime.of(18, 0));
        existing.setReturnTime(LocalTime.of(20, 0)); //ends exactly when the new one starts, no overlap

        when(repository.findByClassroomAndDate(rent.getClassroom(), rent.getDate())).thenReturn(List.of(existing));
        when(repository.findByEquipmentsContainingAndDate(any(), any())).thenReturn(List.of());
        when(repository.save(rent)).thenReturn(rent);

        assertDoesNotThrow(() -> service.save(rent));
    }

    //Rule 1: equipment cannot have overlapping reservations, regardless of classroom

    @Test
    void save_rejectsOverlappingEquipmentReservationAcrossDifferentClassrooms() {
        Equipment equipment = activeEquipment(1L);

        Rent rent = validRent();
        rent.setClassroom(999);
        rent.setEquipments(List.of(equipment));

        Rent existing = validRent();
        existing.setId(5L);
        existing.setClassroom(100); //different room, same equipment/time -> still a conflict
        existing.setTakenTime(LocalTime.of(19, 0));
        existing.setReturnTime(LocalTime.of(21, 0));
        existing.setEquipments(List.of(equipment));

        when(repository.findByClassroomAndDate(rent.getClassroom(), rent.getDate())).thenReturn(List.of());
        when(repository.findByEquipmentsContainingAndDate(equipment, rent.getDate())).thenReturn(List.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.save(rent));
        assertEquals("One or more selected equipment items are already reserved for this time period", ex.getMessage());
    }

    //update() must exclude the reservation being edited from its own conflict checks

    @Test
    void update_excludesItselfFromClassroomConflictCheck() {
        Rent rent = validRent();
        rent.setId(10L);

        when(repository.findById(10L)).thenReturn(Optional.of(validRent()));
        when(repository.findByClassroomAndDate(rent.getClassroom(), rent.getDate())).thenReturn(List.of(rent)); //finds itself
        when(repository.findByEquipmentsContainingAndDate(any(), any())).thenReturn(List.of());
        when(repository.save(any())).thenReturn(rent);

        assertDoesNotThrow(() -> service.update(10L, rent));
    }

    @Test
    void update_excludesItselfFromEquipmentConflictCheck() {
        Equipment equipment = activeEquipment(1L);
        Rent rent = validRent();
        rent.setId(10L);
        rent.setEquipments(List.of(equipment));

        when(repository.findById(10L)).thenReturn(Optional.of(validRent()));
        when(repository.findByClassroomAndDate(anyInt(), any())).thenReturn(List.of());
        when(repository.findByEquipmentsContainingAndDate(equipment, rent.getDate())).thenReturn(List.of(rent)); //finds itself
        when(repository.save(any())).thenReturn(rent);

        assertDoesNotThrow(() -> service.update(10L, rent));
    }
}
