package com.psv.datos_mapas.controller;

import com.psv.datos_mapas.dto.DepartamentoSimpleDTO;
import com.psv.datos_mapas.model.Departamento;
import com.psv.datos_mapas.repository.DepartamentoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departamentos")
@CrossOrigin(origins = "*")
public class DepartamentoController {

    private final DepartamentoRepository repository;

    public DepartamentoController(DepartamentoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<DepartamentoSimpleDTO>> obtenerDepartamentos() {
        List<DepartamentoSimpleDTO> dtos = repository.findAll().stream()
            .map(DepartamentoSimpleDTO::fromEntity)
            .toList();
        return ResponseEntity.ok(dtos);
    }
}