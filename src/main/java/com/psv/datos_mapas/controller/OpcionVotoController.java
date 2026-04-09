package com.psv.datos_mapas.controller;

import com.psv.datos_mapas.model.OpcionVoto;
import com.psv.datos_mapas.repository.OpcionVotoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/opciones-voto")
@CrossOrigin(origins = "*")
public class OpcionVotoController {

    private final OpcionVotoRepository repository;

    public OpcionVotoController(OpcionVotoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<OpcionVoto>> obtenerOpciones() {
        return ResponseEntity.ok(repository.findAll());
    }
}