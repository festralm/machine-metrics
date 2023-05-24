package ru.kpfu.machinemetrics.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.machinemetrics.dto.ScheduleCreateDto;
import ru.kpfu.machinemetrics.dto.ScheduleDto;
import ru.kpfu.machinemetrics.mapper.ScheduleMapper;
import ru.kpfu.machinemetrics.model.Schedule;
import ru.kpfu.machinemetrics.service.ScheduleService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "${app.api.prefix.v1}/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final ScheduleMapper scheduleMapper;

    @GetMapping("/default")
    public List<ScheduleDto> listDefault() {
        List<Schedule> scheduleList = scheduleService.listDefault();
        return scheduleMapper.toScheduleDtos(scheduleList);
    }

    @GetMapping
    public List<ScheduleDto> listNotDefault() {
        List<Schedule> scheduleList = scheduleService.listNotDefault();
        return scheduleMapper.toScheduleDtos(scheduleList);
    }

    @GetMapping("/equipment/{id}")
    public List<ScheduleDto> listByEquipmentId(@PathVariable Long id) {
        List<Schedule> scheduleList = scheduleService.listByEquipmentId(id);
        return scheduleMapper.toScheduleDtos(scheduleList);
    }

    @PostMapping
    public ResponseEntity<ScheduleDto> create(@Valid @RequestBody ScheduleCreateDto dto) {
        Schedule schedule = scheduleMapper.toSchedule(dto);
        Schedule savedSchedule = scheduleService.save(schedule);
        ScheduleDto savedScheduleDto = scheduleMapper.toScheduleDto(savedSchedule);
        return ResponseEntity.created(URI.create("/schedule")).body(savedScheduleDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        scheduleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ScheduleDto edit(@PathVariable Long id, @Valid @RequestBody ScheduleCreateDto scheduleCreateDto) {
        Schedule updatedSchedule = scheduleMapper.toSchedule(scheduleCreateDto);
        Schedule editedSchedule = scheduleService.edit(id, updatedSchedule);
        return scheduleMapper.toScheduleDto(editedSchedule);
    }
}
