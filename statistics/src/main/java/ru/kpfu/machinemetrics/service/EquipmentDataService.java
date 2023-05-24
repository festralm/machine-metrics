package ru.kpfu.machinemetrics.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kpfu.machinemetrics.dto.EquipmentDataDto;
import ru.kpfu.machinemetrics.dto.StatisticsDto;
import ru.kpfu.machinemetrics.mapper.ScheduleMapper;
import ru.kpfu.machinemetrics.model.EquipmentData;
import ru.kpfu.machinemetrics.model.Schedule;
import ru.kpfu.machinemetrics.properties.AppProperties;
import ru.kpfu.machinemetrics.repository.EquipmentDataRepository;
import ru.kpfu.machinemetrics.repository.ScheduleRepository;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.Period;
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

    private final AppProperties appProperties;
    private final EquipmentDataRepository equipmentDataRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;

    private static final DateTimeFormatter isoOffsetDateTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public StatisticsDto getData(Long equipmentId, OffsetDateTime start, OffsetDateTime end) {
        if (start == null) {
            start = OffsetDateTime.now().minus(Period.ofDays(1));
        } else {
            start = start.withOffsetSameInstant(ZoneOffset.of(appProperties.getDefaultZone()));
        }
        if (end == null) {
            end = OffsetDateTime.now();
        } else {
            end = end.withOffsetSameInstant(ZoneOffset.of(appProperties.getDefaultZone()));
        }

        final List<EquipmentData> equipmentData = equipmentDataRepository.getData(
                start.format(isoOffsetDateTime),
                end.format(isoOffsetDateTime),
                equipmentId
        );

        return fillEquipmentListData(equipmentData, equipmentId, start, end);
    }

    private StatisticsDto fillEquipmentListData(
            List<EquipmentData> equipmentDataList,
            Long equipmentId,
            OffsetDateTime start,
            OffsetDateTime end
    ) {
        Duration totalDuration = Duration.between(start, end);

        var result = StatisticsDto.builder()
                .equipmentData(new ArrayList<>())
                .upMinutes(0L)
                .upScheduleMinutes(0L)
                .upNotScheduleMinutes(0L)
                .downScheduleMinutes(0L)
                .start(start)
                .end(end)
                .schedules(new HashMap<>())
                .build();

        var schedules = getSchedules(equipmentId, start, end);

        boolean previousEnabled = false;
        if (equipmentDataList.size() != 0) {
            if (equipmentDataList.get(0).getTime().isBefore(start)) {
                previousEnabled = equipmentDataList.get(0).getEnabled();
                equipmentDataList = equipmentDataList.subList(1, equipmentDataList.size());
            }
        }

        if (equipmentDataList.size() != 0) {
            final EquipmentData lastEquipmentData = equipmentDataList.get(equipmentDataList.size() - 1);

            fillTimeBeforeFirstData(result, previousEnabled, start, schedules, equipmentDataList.get(0).getTime());
            fillTimeAfterLastData(result, lastEquipmentData.getEnabled(), end, schedules, lastEquipmentData.getTime());

            fillFromList(equipmentDataList, start, end, result, schedules, previousEnabled);

            final Long upScheduleMinutes = result.getUpScheduleMinutes();
            final Long downScheduleMinutes = result.getDownScheduleMinutes();

            result.setUpSchedulePercent(upScheduleMinutes * 100.0 / (upScheduleMinutes + downScheduleMinutes));
            result.setDownSchedulePercent(downScheduleMinutes * 100.0 / (upScheduleMinutes + downScheduleMinutes));
        } else {
            if (previousEnabled) {
                result.setUpMinutes(totalDuration.toMinutes());
            }
            fillIfEmpty(start, end, result, schedules, previousEnabled);
        }

        result.setDownMinutes(totalDuration.toMinutes() - result.getUpMinutes());
        result.setUpNotScheduleMinutes(result.getUpMinutes() - result.getUpScheduleMinutes());

        result.setSchedules(
                schedules.entrySet().stream()
                        .collect(
                                Collectors.toMap(Map.Entry::getKey,
                                        e -> scheduleMapper.toScheduleDto(e.getValue()))
                        )
        );
        return result;
    }

    private static void fillTimeBeforeFirstData(
            StatisticsDto result,
            boolean previousEnabled,
            OffsetDateTime start,
            Map<OffsetDateTime, Schedule> schedules,
            OffsetDateTime firstDataDateTime
    ) {
        schedules
                .entrySet()
                .stream()
                .filter(x -> firstDataDateTime.isAfter(x.getKey())
                        || x.getKey().truncatedTo(ChronoUnit.DAYS).equals(firstDataDateTime.truncatedTo(ChronoUnit.DAYS))
                        && firstDataDateTime.isAfter(start)
                )
                .forEach(x -> {
                    final Schedule schedule = x.getValue();

                    long scheduleTime = 0;
                    if (start.truncatedTo(ChronoUnit.DAYS).equals(x.getKey().truncatedTo(ChronoUnit.DAYS))) {
                        if (firstDataDateTime.truncatedTo(ChronoUnit.DAYS).equals(x.getKey().truncatedTo(ChronoUnit.DAYS))) {
                            if (firstDataDateTime.isBefore(getDateTimeFromDate(x.getKey(), schedule.getStartTime()))
                                    || firstDataDateTime.truncatedTo(ChronoUnit.MINUTES).equals(x.getKey().truncatedTo(ChronoUnit.MINUTES))) {
                                scheduleTime = getScheduleMinutes(start, firstDataDateTime, schedule);
                            }
                        } else {
                            scheduleTime = getScheduleMinutes(start, getDateTimeFromDate(x.getKey(), schedule.getEndTime()), schedule);
                        }
                    } else if (firstDataDateTime.truncatedTo(ChronoUnit.DAYS).equals(x.getKey().truncatedTo(ChronoUnit.DAYS))) {
                        scheduleTime = getScheduleMinutes(getDateTimeFromDate(x.getKey(), schedule.getStartTime()), firstDataDateTime, schedule);
                    } else {
                        scheduleTime = schedule.getEndTime().longValue() - schedule.getStartTime();
                    }
                    if (previousEnabled) {
                        result.setUpScheduleMinutes(result.getUpScheduleMinutes() + scheduleTime);
                    } else {
                        result.setDownScheduleMinutes(result.getDownScheduleMinutes() + scheduleTime);
                    }
                });
    }

    private static void fillTimeAfterLastData(
            StatisticsDto result,
            boolean lastEnabled,
            OffsetDateTime end,
            Map<OffsetDateTime, Schedule> schedules,
            OffsetDateTime lastDataDateTime
    ) {
        schedules
                .entrySet()
                .stream()
                .filter(x -> lastDataDateTime.isBefore(x.getKey())
                        || x.getKey().truncatedTo(ChronoUnit.DAYS).equals(lastDataDateTime.truncatedTo(ChronoUnit.DAYS))
                        && lastDataDateTime.isBefore(end)
                )
                .forEach(x -> {
                    final Schedule schedule = x.getValue();

                    long scheduleTime = 0;

                    if (end.truncatedTo(ChronoUnit.DAYS).equals(x.getKey().truncatedTo(ChronoUnit.DAYS))) {
                        if (lastDataDateTime.truncatedTo(ChronoUnit.DAYS).equals(x.getKey().truncatedTo(ChronoUnit.DAYS))) {
                            if (lastDataDateTime.isAfter(getDateTimeFromDate(x.getKey(), schedule.getEndTime()))
                                    || lastDataDateTime.truncatedTo(ChronoUnit.MINUTES).equals(x.getKey().truncatedTo(ChronoUnit.MINUTES))) {
                                scheduleTime = getScheduleMinutes(lastDataDateTime, end, schedule);
                            }
                        } else {
                            scheduleTime = getScheduleMinutes(getDateTimeFromDate(x.getKey(), schedule.getStartTime()), end, schedule);
                        }
                    } else if (lastDataDateTime.truncatedTo(ChronoUnit.DAYS).equals(x.getKey().truncatedTo(ChronoUnit.DAYS))) {
                        scheduleTime = getScheduleMinutes(lastDataDateTime, getDateTimeFromDate(x.getKey(), schedule.getEndTime()), schedule);
                    } else {
                        scheduleTime = schedule.getEndTime().longValue() - schedule.getStartTime();
                    }

                    if (lastEnabled) {
                        result.setUpScheduleMinutes(result.getUpScheduleMinutes() + scheduleTime);
                    } else {
                        result.setDownScheduleMinutes(result.getDownScheduleMinutes() + scheduleTime);
                    }
                });
    }

    private static void fillTimeBetweenSchedules(
            StatisticsDto result,
            boolean lastEnabled,
            boolean nextEnabled,
            Map<OffsetDateTime, Schedule> schedules,
            OffsetDateTime leftDate,
            OffsetDateTime rightDate
    ) {
        fillTimeAfterLastData(result, lastEnabled, leftDate, schedules, rightDate);
    }

    @org.jetbrains.annotations.NotNull
    private static OffsetDateTime getDateTimeFromDate(OffsetDateTime dateTime, Integer time) {
        return dateTime.withHour(time / 60).withMinute(time % 60);
    }

    private void fillFromList(
            List<EquipmentData> equipmentDataList,
            OffsetDateTime start,
            OffsetDateTime end,
            StatisticsDto result,
            Map<OffsetDateTime, Schedule> schedules,
            boolean previousEnabled
    ) {
        OffsetDateTime previousTime = start;

        boolean isFirst = true;

        EquipmentData lastEquipmentData = null;
        for (EquipmentData equipmentData : equipmentDataList) {
            calculate(result, schedules, equipmentData, previousTime, previousEnabled, null, isFirst);

            previousTime = equipmentData.getTime();
            previousEnabled = equipmentData.getEnabled();
            lastEquipmentData = equipmentData;
            isFirst = false;
        }

        if (lastEquipmentData != null) {
            calculate(result, schedules, lastEquipmentData, previousTime, previousEnabled, end, false);
        }
    }

    private static void fillIfEmpty(
            OffsetDateTime start,
            OffsetDateTime end,
            StatisticsDto result,
            Map<OffsetDateTime, Schedule> schedules,
            boolean previousEnabled
    ) {
        final Long scheduleMinutes = schedules.entrySet()
                .stream()
                .map(x -> {
                    final int startTime = getTimeFromDateTime(start);
                    if (start.truncatedTo(ChronoUnit.DAYS).equals(x.getKey().truncatedTo(ChronoUnit.DAYS))
                            && timeIsInSchedule(startTime, x.getValue())) {
                        return Duration.between(
                                start,
                                getDateTimeWithMinute(start, x.getValue().getEndTime())
                        ).toMinutes();
                    }
                    final int endTime = getTimeFromDateTime(end);
                    if (end.truncatedTo(ChronoUnit.DAYS).equals(x.getKey().truncatedTo(ChronoUnit.DAYS))
                            && timeIsInSchedule(endTime, x.getValue())) {
                        return Duration.between(
                                getDateTimeWithMinute(end, x.getValue().getStartTime()),
                                end
                        ).toMinutes();
                    }
                    return Duration.between(
                            getDateTimeFromDate(x.getKey(), x.getValue().getStartTime()),
                            getDateTimeFromDate(x.getKey(), x.getValue().getEndTime())
                    ).toMinutes();
                })
                .reduce(0L, Long::sum);
        if (previousEnabled) {
            result.setUpScheduleMinutes(scheduleMinutes);
            result.setUpSchedulePercent(100.0);
            result.setDownSchedulePercent(0.0);
        } else {
            result.setDownScheduleMinutes(scheduleMinutes);
            result.setUpSchedulePercent(0.0);
            result.setDownSchedulePercent(100.0);
        }
    }

    private Map<OffsetDateTime, Schedule> getSchedules(Long equipmentId, OffsetDateTime start, OffsetDateTime end) {
        Map<OffsetDateTime, Schedule> result = new HashMap<>();
        for (OffsetDateTime date = start; date.isBefore(end); date = date.plusDays(1)) {
            Schedule schedule = getSchedule(equipmentId, date);
            result.put(date.truncatedTo(ChronoUnit.DAYS), schedule);
        }
        return result;
    }

    private void calculate(
            StatisticsDto dto,
            Map<OffsetDateTime, Schedule> schedules,
            EquipmentData equipmentData,
            OffsetDateTime previousDateTime,
            boolean previousEnabled,
            OffsetDateTime end,
            boolean isFirst
    ) {
        OffsetDateTime currentDateTime = end == null ? equipmentData.getTime() : end;

        Schedule schedule = schedules.get(currentDateTime.truncatedTo(ChronoUnit.DAYS));

        if (end == null) {
            EquipmentDataDto equipmentDataDto = getEquipmentDataDto(schedule, equipmentData);
            dto.getEquipmentData().add(equipmentDataDto);
        }

        if (previousEnabled) {
            var totalMinutes = Duration.between(previousDateTime, currentDateTime).toMinutes();
            dto.setUpMinutes(dto.getUpMinutes() + totalMinutes);
        }

        if (previousDateTime.truncatedTo(ChronoUnit.DAYS).equals(currentDateTime.truncatedTo(ChronoUnit.DAYS))) {
            long scheduleMinutes = getScheduleMinutes(previousDateTime, currentDateTime, schedule);
            if (previousEnabled) {
                dto.setUpScheduleMinutes(dto.getUpScheduleMinutes() + scheduleMinutes);
            } else if (!previousDateTime.equals(currentDateTime)) {
                dto.setDownScheduleMinutes(dto.getDownScheduleMinutes() + scheduleMinutes);
            }
        } else if (end == null && !isFirst) {
            Map<OffsetDateTime, Schedule> schedulesBetween = new HashMap<>();
            for (OffsetDateTime date = previousDateTime; date.isBefore(currentDateTime); date = date.plusDays(1)) {
                schedulesBetween.put(date, schedules.get(date.truncatedTo(ChronoUnit.DAYS)));
            }
            fillTimeBetweenSchedules(
                    dto,
                    previousEnabled,
                    equipmentData.getEnabled(),
                    schedulesBetween,
                    currentDateTime,
                    previousDateTime
            );
        }
    }

    private static int getTimeFromDateTime(OffsetDateTime currentDateTime) {
        return currentDateTime.getHour() * 60 + currentDateTime.getMinute();
    }

    private static long getScheduleMinutes(
            OffsetDateTime startDateTime,
            OffsetDateTime endDateTime,
            Schedule schedule
    ) {
        int startTime = getTimeFromDateTime(startDateTime);
        int endTime = getTimeFromDateTime(endDateTime);

        OffsetDateTime scheduleStartDateTime = getDateTimeWithMinute(startDateTime, schedule.getStartTime());
        OffsetDateTime scheduleEndDateTime = getDateTimeWithMinute(startDateTime, schedule.getEndTime());

        if (timeIsInSchedule(startTime, schedule) && timeIsInSchedule(endTime, schedule)) {
            return endTime - startTime;
        } else if (timeIsInSchedule(startTime, schedule)) {
            return Duration.between(
                    startDateTime,
                    scheduleEndDateTime
            ).toMinutes();
        } else {
            if (timeIsBeforeSchedule(startTime, schedule)) {
                if (timeIsInSchedule(endTime, schedule)) {
                    return Duration.between(
                            scheduleStartDateTime,
                            endDateTime
                    ).toMinutes();
                } else if (timeIsAfterSchedule(endTime, schedule)) {
                    return Duration.between(
                            scheduleStartDateTime,
                            scheduleEndDateTime
                    ).toMinutes();
                }
            }
        }
        return 0;
    }

    private static boolean timeIsAfterSchedule(int currentTime, Schedule schedule) {
        return currentTime >= schedule.getEndTime();
    }

    private static boolean timeIsBeforeSchedule(int previousTime, Schedule schedule) {
        return previousTime <= schedule.getStartTime();
    }

    private static boolean timeIsInSchedule(int previousTime, Schedule schedule) {
        return schedule.getStartTime() <= previousTime && previousTime <= schedule.getEndTime();
    }

    @org.jetbrains.annotations.NotNull
    private static OffsetDateTime getDateTimeWithMinute(OffsetDateTime date, Integer time) {
        return date.withHour(time / 60).withMinute(time % 60);
    }

    private Schedule getSchedule(Long equipmentId, OffsetDateTime currentTime) {
        Schedule schedule;
        List<Schedule> scheduleList = scheduleRepository.findAllByDateAndEquipmentId(
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
                scheduleList = scheduleRepository.findAllByDateAndEquipmentId(
                        null,
                        equipmentId
                );
                if (scheduleList.size() > 0) {
                    schedule = scheduleList.get(0);
                } else {
                    scheduleList = scheduleRepository.findAllByDateAndEquipmentId(
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
                            scheduleList = scheduleRepository.findAllByDateAndEquipmentId(
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

    private EquipmentDataDto getEquipmentDataDto(Schedule schedule, EquipmentData equipmentData) {
        final OffsetDateTime dataTime = equipmentData.getTime();
        final Boolean enabled = equipmentData.getEnabled();
        EquipmentDataDto result = EquipmentDataDto.builder()
                .u(equipmentData.getU())
                .enabled(enabled)
                .equipmentId(equipmentData.getEquipmentId())
                .time(dataTime)
                .build();

        var hour = dataTime.getHour();
        var minute = dataTime.getMinute();
        var totalMinutes = hour * 60 + minute;

        var hourInSchedule = totalMinutes >= schedule.getStartTime() && totalMinutes <= schedule.getEndTime();

        result.setDisabledDuringActiveTime(hourInSchedule && !enabled);
        result.setEnabledDuringPassiveTime(!hourInSchedule && enabled);

        return result;
    }

    public void delete(@NotNull Long equipmentId) {
        equipmentDataRepository.delete(equipmentId);
        scheduleRepository.deleteAllByEquipmentId(equipmentId);
    }
}
