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
import ru.kpfu.machinemetrics.model.Purpose;
import ru.kpfu.machinemetrics.service.PurposeService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "${app.api.prefix.v1}/purpose")
@RequiredArgsConstructor
public class PurposeController {

    private final PurposeService purposeService;

    @GetMapping
    public List<Purpose> listAll() {
        return purposeService.getAll();
    }

    @PostMapping
    public ResponseEntity<Purpose> create(@Valid @RequestBody Purpose purpose) {
        Purpose savedPurpose = purposeService.save(purpose);
        return ResponseEntity.created(URI.create("/purpose")).body(savedPurpose);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        purposeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
