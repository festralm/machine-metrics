package ru.kpfu.machinemetrics.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kpfu.machinemetrics.dto.EquipmentDataDto;
import ru.kpfu.machinemetrics.dto.ScheduleDto;
import ru.kpfu.machinemetrics.dto.StatisticsDto;
import ru.kpfu.machinemetrics.mapper.ScheduleMapper;
import ru.kpfu.machinemetrics.model.EquipmentData;
import ru.kpfu.machinemetrics.model.Schedule;
import ru.kpfu.machinemetrics.properties.AppProperties;
import ru.kpfu.machinemetrics.repository.EquipmentDataRepository;
import ru.kpfu.machinemetrics.repository.ScheduleRepository;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentDataService {

    private final AppProperties appProperties;
    private final EquipmentDataRepository equipmentDataRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;

    public StatisticsDto getData(Long equipmentId, OffsetDateTime start, OffsetDateTime end) {
        if (start == null) {
            start = ZonedDateTime.now().minus(Period.ofDays(1)).toOffsetDateTime();
        }
        if (end == null) {
            end = OffsetDateTime.now();
        }

        final List<EquipmentData> equipmentData = equipmentDataRepository.getData(
                start.toString(),
                end.toString(),
                equipmentId
        );

        // todo query the last previous and retrieve if it was turned on
        return fillEquipmentListData(equipmentData, start, end);
    }

    private StatisticsDto fillEquipmentListData(List<EquipmentData> equipmentDataList, OffsetDateTime start, OffsetDateTime end) {
        Duration totalDuration = Duration.between(start, end);

        Duration totalUpDuration = Duration.ZERO;
        OffsetDateTime previousTime = null;
        boolean previousEnabled = false;

        Optional<Schedule> defaultScheduleOpt = scheduleRepository.findByDate(null);
        List<EquipmentDataDto> equipmentDataDtoList = new ArrayList<>();

        final List<ScheduleDto> schedules = new ArrayList<>();

        defaultScheduleOpt.ifPresent(
                defaultSchedule -> schedules.add(scheduleMapper.toScheduleDto(defaultSchedule))
        );

        // todo count normally
        for (EquipmentData equipmentData : equipmentDataList) {
            OffsetDateTime currentTime = equipmentData.getTime();

            EquipmentDataDto equipmentDataDto = getEquipmentDataDto(defaultScheduleOpt, equipmentData);
            equipmentDataDtoList.add(equipmentDataDto);

            if (previousEnabled) {
                Duration duration;
                OffsetDateTime time;
                time = Objects.requireNonNullElse(previousTime, start);
                final OffsetDateTime oldCurrent = currentTime;
                if (defaultScheduleOpt.isPresent()) {
                    var defaultSchedule = defaultScheduleOpt.get();
                    final ZoneId zoneId = ZoneId.of(appProperties.getDefaultZone());

                    final LocalTime localTime = time.toLocalTime();
                    if (localTime.getHour() * 60 + localTime.getMinute() <= defaultSchedule.getStartTime()) {
                        time = LocalTime.of(
                                        defaultSchedule.getStartTime().intValue() / 60,
                                        defaultSchedule.getStartTime().intValue() % 60
                                ).atDate(time.toLocalDate())
                                .atOffset(ZoneOffset.of(appProperties.getDefaultZone()));
                    }

                    final LocalTime localCurrentTime = currentTime.toLocalTime();
                    if (localCurrentTime.getHour() * 60 + localCurrentTime.getMinute() >= defaultSchedule.getEndTime()) {
                        currentTime = LocalTime.of(
                                        defaultSchedule.getEndTime().intValue() / 60,
                                        defaultSchedule.getEndTime().intValue() % 60
                                ).atDate(time.toLocalDate())
                                .atOffset(ZoneOffset.of(appProperties.getDefaultZone()));
                    }
                }
                duration = Duration.between(time, currentTime);
                currentTime = oldCurrent;
                totalUpDuration = totalUpDuration.plus(duration);
            }

            previousTime = currentTime;
            previousEnabled = equipmentData.getEnabled();
        }
        if (previousEnabled) {
            Duration duration = Duration.between(previousTime, end);
            totalUpDuration = totalUpDuration.plus(duration);
        }

        final long upHours = totalUpDuration.toHours();
        final long downHours = totalDuration.minus(totalUpDuration).toHours();
        final long totalHours = totalDuration.toHours();
        return StatisticsDto.builder()
                .equipmentData(equipmentDataDtoList)
                .upHours(upHours)
                .downHours(downHours)
                .totalHours(totalHours)
                .upPercent(upHours * 100.0 / totalHours)
                .start(start)
                .end(end)
                .schedules(schedules)
                .build();
    }

    private EquipmentDataDto getEquipmentDataDto(Optional<Schedule> defaultScheduleOpt, EquipmentData equipmentData) {
        final OffsetDateTime dataTime = equipmentData.getTime();
        final Boolean enabled = equipmentData.getEnabled();
        EquipmentDataDto result = EquipmentDataDto.builder()
                .u(equipmentData.getU())
                .enabled(enabled)
                .equipmentId(equipmentData.getEquipmentId())
                .time(dataTime)
                .build();
        defaultScheduleOpt.ifPresent(defaultSchedule -> {
            var hour = dataTime.getHour();
            var minute = dataTime.getMinute();
            var totalMinutes = hour * 60 + minute;
            var hourInSchedule = totalMinutes >= defaultSchedule.getStartTime()
                    && totalMinutes <= defaultSchedule.getEndTime();
            result.setDisabledDuringActiveTime(hourInSchedule && !enabled);
            result.setEnabledDuringPassiveTime(!hourInSchedule && enabled);
        });
        return result;
    }

    public void delete(@NotNull Long equipmentId) {
        equipmentDataRepository.delete(equipmentId);
        scheduleRepository.deleteAllByEquipmentId(equipmentId);
    }
}
