package ru.kpfu.machinemetrics.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.machinemetrics.model.UsageType;
import ru.kpfu.machinemetrics.service.UsageTypeService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "${app.api.prefix.v1}/usage-type")
@RequiredArgsConstructor
public class UsageTypeController {

    private final UsageTypeService usageTypeService;

    @GetMapping
    public List<UsageType> listAll() {
        return usageTypeService.getAll();
    }

    @PostMapping
    public ResponseEntity<UsageType> create(@Valid @RequestBody UsageType usageType) {
        UsageType savedUsageType = usageTypeService.save(usageType);
        return ResponseEntity.created(URI.create("/usage-type")).body(savedUsageType);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        usageTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
