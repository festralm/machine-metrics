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
import ru.kpfu.machinemetrics.model.DataService;
import ru.kpfu.machinemetrics.service.DataServiceService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "${api.prefix.v1}/data-service")
@RequiredArgsConstructor
public class DataServiceController {

    private final DataServiceService dataServiceService;

    @GetMapping
    public List<DataService> listAll() {
        return dataServiceService.getAll();
    }

    @PostMapping
    public ResponseEntity<DataService> create(@Valid @RequestBody DataService dataService) {
        DataService savedDataService = dataServiceService.save(dataService);
        return ResponseEntity.created(URI.create("/data-service/" + savedDataService.getId())).body(savedDataService);
    }

    @GetMapping("/{id}")
    public DataService get(@PathVariable Long id) {
        return dataServiceService.getById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dataServiceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping
    public DataService edit(@Valid @RequestBody DataService dataService) {
        return dataServiceService.edit(dataService);
    }
}
