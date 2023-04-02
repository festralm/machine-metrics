package ru.kpfu.equipment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.kpfu.equipment.model.Equipment;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findAllByDeletedFalse();

    Optional<Equipment> findByIdAndDeletedFalse(Long id);
}
