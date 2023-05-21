package ru.kpfu.machinemetrics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.kpfu.machinemetrics.model.EquipmentSchedule;

import java.util.List;

@Repository
public interface EquipmentScheduleRepository extends JpaRepository<EquipmentSchedule, Long> {

    List<EquipmentSchedule> findAllByEnabledIsTrue();
}
