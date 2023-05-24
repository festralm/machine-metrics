package ru.kpfu.machinemetrics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.kpfu.machinemetrics.model.Schedule;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    Optional<Schedule> findByDateAndEquipmentIdAndWeekday(OffsetDateTime OffsetDateTime, Long equipmentId, Integer weekday);

    List<Schedule> findAllByDateAndEquipmentId(OffsetDateTime OffsetDateTime, Long equipmentId);

    List<Schedule> findAllByWeekdayAndEquipmentId(Integer weekday, Long equipmentId);

    List<Schedule> findAllByEquipmentId(Long equipmentId);

    @Query(value = "select schedule from Schedule schedule where schedule.date is not null or schedule.equipmentId is not null")
    List<Schedule> findAllNotDefault();

    Optional<Schedule> findByDate(OffsetDateTime OffsetDateTime);

    void deleteAllByEquipmentId(Long id);
}
