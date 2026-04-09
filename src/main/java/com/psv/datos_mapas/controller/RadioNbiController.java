package com.psv.datos_mapas.controller;

import com.psv.datos_mapas.service.RadioNbiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/radios")
@CrossOrigin(origins = "*")
public class RadioNbiController {

    private final RadioNbiService service;

    public RadioNbiController(RadioNbiService service) {
        this.service = service;
    }

    @GetMapping("/nbi")
    public ResponseEntity<Map<String, Object>> obtenerRadiosConNbi(
            @RequestParam(required = false) List<Integer> departamentoIds) {
        return ResponseEntity.ok(service.obtenerRadiosConNbi(departamentoIds));
    }
}