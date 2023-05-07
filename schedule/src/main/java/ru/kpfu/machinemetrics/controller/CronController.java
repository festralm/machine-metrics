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
import ru.kpfu.machinemetrics.dto.CronCreateDto;
import ru.kpfu.machinemetrics.mapper.CronMapper;
import ru.kpfu.machinemetrics.model.Cron;
import ru.kpfu.machinemetrics.service.CronService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "${api.prefix.v1}/cron")
@RequiredArgsConstructor
public class CronController {

    private final CronService cronService;
    private final CronMapper cronMapper;

    @GetMapping
    public List<Cron> listAll() {
        return cronService.getAll();
    }

    @PostMapping
    public ResponseEntity<Cron> create(@Valid @RequestBody CronCreateDto cronDto) {
        Cron cron = cronMapper.toCron(cronDto);
        Cron savedCron = cronService.save(cron);
        return ResponseEntity.created(URI.create("/cron")).body(savedCron);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cronService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public Cron edit(@PathVariable Long id,
                     @Valid @RequestBody CronCreateDto cronCreateDto) {
        Cron cron = cronMapper.toCron(cronCreateDto);
        return cronService.edit(id, cron);
    }
}
