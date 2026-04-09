package com.psv.datos_mapas.service;

import com.psv.datos_mapas.model.RadioCensal;
import com.psv.datos_mapas.repository.RadioCensalRepository;
import com.psv.datos_mapas.util.GeoJsonHelper;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RadioNbiService {

    private final RadioCensalRepository radioRepository;

    public RadioNbiService(RadioCensalRepository radioRepository) {
        this.radioRepository = radioRepository;
    }

    public Map<String, Object> obtenerRadiosConNbi(List<Integer> departamentoIds) {
        List<RadioCensal> radios = (departamentoIds == null || departamentoIds.isEmpty())
            ? radioRepository.findAll()
            : radioRepository.findByDepartamentoIdIn(departamentoIds);
        
        List<Map<String, Object>> features = new ArrayList<>();
        for (RadioCensal radio : radios) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("id", radio.getId());
            properties.put("codigo", radio.getCodigoINDEC());
            properties.put("departamentoId", radio.getDepartamento() != null ? radio.getDepartamento().getId() : null);
            properties.put("hogaresTotal", radio.getHogaresTotal());
            properties.put("hogaresNbi", radio.getHogaresNBI());
            
            double porcentajeNbi = 0.0;
            if (radio.getHogaresTotal() != null && radio.getHogaresTotal() > 0) {
                porcentajeNbi = (radio.getHogaresNBI() * 100.0) / radio.getHogaresTotal();
            }
            properties.put("porcentajeNbi", Math.round(porcentajeNbi * 100.0) / 100.0);
            
            Geometry geometria = radio.getGeometria();
            if (geometria != null) {
                features.add(GeoJsonHelper.toFeature(geometria, properties));
            }
        }
        
        // Agregar metadata
        Map<String, Object> response = GeoJsonHelper.toFeatureCollection(features);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("departamentoIds", departamentoIds);
        metadata.put("totalRadios", features.size());
        response.put("metadata", metadata);
        
        return response;
    }
}