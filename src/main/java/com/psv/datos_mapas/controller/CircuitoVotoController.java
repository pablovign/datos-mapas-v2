package com.psv.datos_mapas.controller;

import com.psv.datos_mapas.service.CircuitoVotoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/circuitos")
@CrossOrigin(origins = "*")
public class CircuitoVotoController {

    private final CircuitoVotoService service;

    public CircuitoVotoController(CircuitoVotoService service) {
        this.service = service;
    }

    @GetMapping("/votos")
    public ResponseEntity<Map<String, Object>> obtenerCircuitosConVotos(
            @RequestParam(required = false) List<Integer> departamentoIds,
            @RequestParam(required = false, defaultValue = "AFIRMATIVOS") String universo,
            @RequestParam(required = false) Integer opcionVotoId) {
        return ResponseEntity.ok(service.obtenerCircuitosConVotos(departamentoIds, universo, opcionVotoId));
    }
}