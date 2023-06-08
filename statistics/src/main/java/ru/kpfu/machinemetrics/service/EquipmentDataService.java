package ru.kpfu.machinemetrics.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kpfu.machinemetrics.dto.StatisticsDto;
import ru.kpfu.machinemetrics.mapper.ScheduleMapper;
import ru.kpfu.machinemetrics.model.Schedule;
import ru.kpfu.machinemetrics.properties.AppProperties;
import ru.kpfu.machinemetrics.repository.EquipmentDataRepository;
import ru.kpfu.machinemetrics.repository.ScheduleRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentDataService {

    private static final DateTimeFormatter isoOffsetDateTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private final AppProperties appProperties;
    private final EquipmentDataRepository equipmentDataRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;

    private static int getTimeFromDateTime(OffsetDateTime currentDateTime) {
        return currentDateTime.getHour() * 60 + currentDateTime.getMinute();
    }


    public StatisticsDto getData(List<Long> equipmentIds, OffsetDateTime start, OffsetDateTime end) {
        if (start == null) {
            start = OffsetDateTime.now().minusHours(1).truncatedTo(ChronoUnit.MINUTES);
            start = start.withMinute((start.getMinute() / 5) * 5);
        } else {
            start = start.withOffsetSameInstant(ZoneOffset.of(appProperties.getDefaultZone()));
        }
        if (end == null) {
            end = OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES);
            end = end.withMinute((start.getMinute() / 5) * 5);
        } else {
            end = end.withOffsetSameInstant(ZoneOffset.of(appProperties.getDefaultZone()));
        }

        var result = StatisticsDto.builder().start(start).end(end).equipmentStatisticsDtos(new ArrayList<>()).build();
        for (var equipmentId : equipmentIds) {
            var equipmentData = equipmentDataRepository.getData(
                    start.format(isoOffsetDateTime),
                    end.format(isoOffsetDateTime),
                    equipmentId
            );
            var schedules = getSchedules(equipmentId, start, end);
            var dto = StatisticsCalculator.calculate(equipmentData, schedules, start, end);
            dto.setSchedules(
                    schedules.entrySet().stream()
                            .collect(
                                    Collectors.toMap(Map.Entry::getKey, e -> scheduleMapper.toScheduleDto(e.getValue()))
                            )
            );
            result.getEquipmentStatisticsDtos().add(dto);
        }

        return result;
    }


    private Map<OffsetDateTime, Schedule> getSchedules(Long equipmentId, OffsetDateTime start, OffsetDateTime end) {
        Map<OffsetDateTime, Schedule> result = new HashMap<>();
        final OffsetDateTime startDay = start.truncatedTo(ChronoUnit.DAYS);
        final OffsetDateTime endDate = end.truncatedTo(ChronoUnit.DAYS);
        for (
                OffsetDateTime date = startDay;
                date.truncatedTo(ChronoUnit.DAYS).isBefore(endDate) || date.truncatedTo(ChronoUnit.DAYS).equals(endDate);
                date = date.plusDays(1)
        ) {
            final OffsetDateTime dateDate = date.truncatedTo(ChronoUnit.DAYS);
            Schedule schedule = getSchedule(equipmentId, dateDate);
            result.put(dateDate, schedule);
        }
        return result;
    }

    private Schedule getSchedule(Long equipmentId, OffsetDateTime currentTime) {
        Schedule schedule;
        List<Schedule> scheduleList = scheduleRepository.findAllByDateAndEquipmentIdOrderByDateAscWeekdayAsc(
                currentTime.truncatedTo(ChronoUnit.DAYS),
                equipmentId
        );
        if (scheduleList.size() > 0) {
            schedule = scheduleList.get(0);
        } else {
            scheduleList = scheduleRepository.findAllByWeekdayAndEquipmentId(
                    currentTime.getDayOfWeek().getValue(),
                    equipmentId
            );
            if (scheduleList.size() > 0) {
                schedule = scheduleList.get(0);
            } else {
                scheduleList = scheduleRepository.findAllByDateAndEquipmentIdOrderByDateAscWeekdayAsc(
                        null,
                        equipmentId
                );
                if (scheduleList.size() > 0) {
                    schedule = scheduleList.get(0);
                } else {
                    scheduleList = scheduleRepository.findAllByDateAndEquipmentIdOrderByDateAscWeekdayAsc(
                            currentTime.truncatedTo(ChronoUnit.DAYS),
                            null
                    );
                    if (scheduleList.size() > 0) {
                        schedule = scheduleList.get(0);
                    } else {
                        scheduleList = scheduleRepository.findAllByWeekdayAndEquipmentId(
                                currentTime.getDayOfWeek().getValue(),
                                null
                        );
                        if (scheduleList.size() > 0) {
                            schedule = scheduleList.get(0);
                        } else {
                            scheduleList = scheduleRepository.findAllByDateAndEquipmentIdOrderByDateAscWeekdayAsc(
                                    null,
                                    null
                            );
                            schedule = scheduleList.get(0);
                        }
                    }
                }
            }
        }
        return schedule;
    }

    public void delete(@NotNull Long equipmentId) {
        equipmentDataRepository.delete(equipmentId);
        scheduleRepository.deleteAllByEquipmentId(equipmentId);
    }
}
