package ru.kpfu.machinemetrics.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.kpfu.machinemetrics.model.Equipment;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findAllByDeletedFalseOrderByName();

    Page<Equipment> findAllByDeletedFalseOrderByName(Pageable pageable);

    Optional<Equipment> findByIdAndDeletedFalse(Long id);

    @Query(value = "select equipment from Equipment equipment where lower(equipment.name) like lower(concat('%', :name, '%'))")
    Page<Equipment> searchAllByNameContainingOrderByName(String name, Pageable pageable);
}
