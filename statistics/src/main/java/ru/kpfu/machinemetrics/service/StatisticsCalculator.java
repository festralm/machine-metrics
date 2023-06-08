package ru.kpfu.machinemetrics.service;

import ru.kpfu.machinemetrics.dto.EquipmentDataDto;
import ru.kpfu.machinemetrics.dto.EquipmentStatisticsDto;
import ru.kpfu.machinemetrics.model.EquipmentData;
import ru.kpfu.machinemetrics.model.Schedule;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Map;

public class StatisticsCalculator {

    public static EquipmentStatisticsDto calculate(
            ArrayList<EquipmentData> equipmentDataList,
            Map<OffsetDateTime, Schedule> dateToSchedule,
            OffsetDateTime start,
            OffsetDateTime end
    ) {
        fillDurations(equipmentDataList, start, end);

        var totalMinutes = Duration.between(start, end).toMinutes();

        var upDuration = Duration.ZERO;
        var upScheduleDuration = Duration.ZERO;
        var downScheduleDuration = Duration.ZERO;

        var equipmentDataDtoList = new ArrayList<EquipmentDataDto>();

        for (var schedule : dateToSchedule.entrySet()) {
            var scheduleStart = getDateTimeWithMinute(schedule.getKey(), schedule.getValue().getStartTime());
            var scheduleEnd = getDateTimeWithMinute(schedule.getKey(), schedule.getValue().getEndTime());

            var currentDateData = equipmentDataList.stream()
                    .filter(x -> x.getTime().truncatedTo(ChronoUnit.DAYS).equals(schedule.getKey().truncatedTo(ChronoUnit.DAYS)))
                    .toList();

            if (currentDateData.size() == 0) {
                var periodStart = scheduleStart.isAfter(start) ? scheduleStart : start;
                var periodEnd = scheduleEnd.isBefore(end) ? scheduleEnd : end;
                downScheduleDuration = downScheduleDuration.plus(Duration.between(periodStart, periodEnd));
            } else {
                for (var metric : currentDateData) {
                    if (metric.isReal() && !metric.getTime().isBefore(start)) {
                        EquipmentDataDto equipmentDataDto = getEquipmentDataDto(schedule.getValue(), metric);
                        equipmentDataDtoList.add(equipmentDataDto);
                    }

                    var metricStart = metric.getTime();
                    var metricEnd = metric.getTime().plus(metric.getDuration());

                    var periodStart = metricStart.isAfter(start) ? metricStart : start;
                    var periodEnd = metricEnd.isBefore(end) ? metricEnd : end;

                    if (metric.getEnabled()) {
                        upDuration = upDuration.plus(Duration.between(periodStart, periodEnd));
                    }

                    // Check if there is an overlap between the schedule and metric times
                    if (metricStart.isBefore(scheduleEnd) && metricEnd.isAfter(scheduleStart)) {
                        var overlapStart = scheduleStart.isAfter(periodStart) ? scheduleStart : periodStart;
                        var overlapEnd = scheduleEnd.isBefore(periodEnd) ? scheduleEnd : periodEnd;
                        var overlapDuration = Duration.between(overlapStart, overlapEnd);

                        if (metric.getEnabled()) {
                            upScheduleDuration = upScheduleDuration.plus(overlapDuration);
                        } else {
                            downScheduleDuration = downScheduleDuration.plus(overlapDuration);
                        }
                    }
                }
            }
        }

        var upMinutes = upDuration.toMinutes();
        var downMinutes = totalMinutes - upMinutes;
        var upScheduleMinutes = upScheduleDuration.toMinutes();
        var upNotScheduleMinutes = upMinutes - upScheduleMinutes;
        var downScheduleMinutes = downScheduleDuration.toMinutes();
        var downNotScheduleMinutes = downMinutes - downScheduleMinutes;

        double upSchedulePercent;
        if (upScheduleMinutes + downScheduleMinutes == 0)      {
            upSchedulePercent = 100;
        } else {
            upSchedulePercent = (100.0 * upScheduleMinutes) / (upScheduleMinutes + downScheduleMinutes);
        }

        equipmentDataDtoList.sort((x, y) -> x.getTime().compareTo(y.getTime()));
        return EquipmentStatisticsDto.builder()
                .equipmentData(equipmentDataDtoList)
                .upMinutes(upMinutes)
                .downMinutes(downMinutes)
                .upScheduleMinutes(upScheduleMinutes)
                .downScheduleMinutes(downScheduleMinutes)
                .upNotScheduleMinutes(upNotScheduleMinutes)
                .downNotScheduleMinutes(downNotScheduleMinutes)
                .upSchedulePercent(upSchedulePercent)
                .downSchedulePercent(100.0 - upSchedulePercent)
                .build();
    }

    private static void fillDurations(ArrayList<EquipmentData> equipmentDataList, OffsetDateTime start, OffsetDateTime end) {
        if (equipmentDataList.size() == 0) {
            return;
        }

        if (equipmentDataList.get(0).getTime().isAfter(start)) {
            equipmentDataList.add(0, EquipmentData.builder().enabled(false).isReal(false).time(start).build());
        }

        if (equipmentDataList.get(equipmentDataList.size() - 1).getTime().isBefore(end)) {
            equipmentDataList.add(
                    EquipmentData.builder()
                            .enabled(equipmentDataList.get(equipmentDataList.size() - 1).getEnabled())
                            .isReal(false)
                            .time(end)
                            .build()
            );
        }

        var copy = new ArrayList<>(equipmentDataList);
        int j = 0;
        for (int i = 0; i < copy.size() - 1; i++) {
            j++;
            var dto = copy.get(i);

            var metricStart = dto.getTime();
            var metricEnd = dto.getTime().plus(Duration.between(dto.getTime(), copy.get(i + 1).getTime()));

            final OffsetDateTime metricStartDays = metricStart.truncatedTo(ChronoUnit.DAYS);
            final OffsetDateTime metricEndDays = metricEnd.truncatedTo(ChronoUnit.DAYS);
            if (!metricStartDays.equals(metricEndDays)) {
                for (var date = metricStartDays; date.isBefore(metricEndDays); date = date.plusDays(1)) {
                    equipmentDataList.add(
                            j,
                            EquipmentData.builder()
                                    .enabled(dto.getEnabled())
                                    .isReal(false)
                                    .time(date.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0))
                                    .build()
                    );
                    j++;
                }
            }
        }

        for (int i = 0; i < equipmentDataList.size() - 1; i++) {
            equipmentDataList.get(i).setDuration(Duration.between(equipmentDataList.get(i).getTime(), equipmentDataList.get(i + 1).getTime()));
        }
        equipmentDataList.get(equipmentDataList.size() - 1).setDuration(Duration.ZERO);
    }

    @org.jetbrains.annotations.NotNull
    private static OffsetDateTime getDateTimeWithMinute(OffsetDateTime date, Integer time) {
        return date.withHour(time / 60).withMinute(time % 60);
    }

    private static EquipmentDataDto getEquipmentDataDto(Schedule schedule, EquipmentData equipmentData) {
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
}
