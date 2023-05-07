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
import ru.kpfu.machinemetrics.model.Cron;
import ru.kpfu.machinemetrics.model.DataService;
import ru.kpfu.machinemetrics.service.CronService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "${api.prefix.v1}/cron")
@RequiredArgsConstructor
public class CronController {

    private final CronService cronService;

    @GetMapping
    public List<Cron> listAll() {
        return cronService.getAll();
    }

    @PostMapping
    public ResponseEntity<Cron> create(@Valid @RequestBody Cron cron) {
        Cron savedCron = cronService.save(cron);
        return ResponseEntity.created(URI.create("/cron")).body(savedCron);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cronService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
