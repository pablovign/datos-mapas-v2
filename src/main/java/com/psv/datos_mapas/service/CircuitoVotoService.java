package com.psv.datos_mapas.service;

import com.psv.datos_mapas.model.CircuitoElectoral;
import com.psv.datos_mapas.model.CircuitoElectoralOpcionVoto;
import com.psv.datos_mapas.model.RadioCensal;
import com.psv.datos_mapas.model.RadioCircuitoIntersec;
import com.psv.datos_mapas.repository.CircuitoElectoralRepository;
import com.psv.datos_mapas.repository.CircuitoElectoralOpcionVotoRepository;
import com.psv.datos_mapas.repository.RadioCircuitoIntersecRepository;
import com.psv.datos_mapas.util.GeoJsonHelper;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CircuitoVotoService {

    private final CircuitoElectoralRepository circuitoRepository;
    private final CircuitoElectoralOpcionVotoRepository votoRepository;
    private final RadioCircuitoIntersecRepository intersecRepository;

    public CircuitoVotoService(CircuitoElectoralRepository circuitoRepository,
                           CircuitoElectoralOpcionVotoRepository votoRepository,
                           RadioCircuitoIntersecRepository intersecRepository) {
        this.circuitoRepository = circuitoRepository;
        this.votoRepository = votoRepository;
        this.intersecRepository = intersecRepository;
    }

    public Map<String, Object> obtenerCircuitosConVotos(List<Integer> departamentoIds, 
                                                     String universo,
                                                     Integer opcionVotoId) {
        // Obtener circuitos filtrados
        List<CircuitoElectoral> circuitos = (departamentoIds == null || departamentoIds.isEmpty())
            ? circuitoRepository.findAll()
            : circuitoRepository.findByDepartamentoIdIn(departamentoIds);

        // Pre-cargar todos los datos de votos
        Map<Integer, List<CircuitoElectoralOpcionVoto>> votosPorCircuito = new HashMap<>();
        for (CircuitoElectoral c : circuitos) {
            votosPorCircuito.put(c.getId(), votoRepository.findByCircuitoElectoralId(c.getId()));
        }

        // Pre-cargar datos de intersección para NBI
        Map<Integer, Double[]> nbiPorCircuito = calcularNbiPorCircuito(circuitos);

        // Procesar cada circuito
        List<Map<String, Object>> features = new ArrayList<>();

        for (CircuitoElectoral circuito : circuitos) {
            Integer circId = circuito.getId();
            List<CircuitoElectoralOpcionVoto> votosCircuito = votosPorCircuito.getOrDefault(circId, Collections.emptyList());

            // Calcular denominador según universo
            int denominador = calcularDenominador(votosCircuito, universo, circuito.getElectores());

            // Calcular % de votos para la opción elegida
            Double porcentajeVoto = null;
            if (opcionVotoId != null && denominador > 0) {
                Integer votosOpcion = votosCircuito.stream()
                    .filter(v -> v.getOpcionVoto().getId().equals(opcionVotoId))
                    .findFirst()
                    .map(CircuitoElectoralOpcionVoto::getCantidad)
                    .orElse(0);
                
                // Si es abstención (opción especial) y universo es HABILITADOS
                if (opcionVotoId == -1 && universo.equals("HABILITADOS")) {
                    int electores = circuito.getElectores() != null ? circuito.getElectores() : 0;
                    int votosEmitidos = votosCircuito.stream()
                        .map(CircuitoElectoralOpcionVoto::getCantidad)
                        .filter(Objects::nonNull)
                        .mapToInt(Integer::intValue)
                        .sum();
                    votosOpcion = Math.max(electores - votosEmitidos, 0);
                }
                
                porcentajeVoto = (votosOpcion * 100.0) / denominador;
                porcentajeVoto = redondearDosDecimales(porcentajeVoto);
            }

            // Obtener % NBI del circuito
            Double[] nbiData = nbiPorCircuito.get(circId);
            Double hogaresNbiPonderado = (nbiData != null) ? nbiData[0] : 0.0;
            Double hogaresPonderados = (nbiData != null) ? nbiData[1] : 0.0;
            Double porcentajeNbi = (nbiData != null && nbiData[1] > 0) 
                ? (nbiData[0] / nbiData[1]) * 100.0 
                : 0.0;
            porcentajeNbi = redondearDosDecimales(porcentajeNbi);

            // Crear feature
            Map<String, Object> properties = new HashMap<>();
            properties.put("id", circId);
            properties.put("codigo", circuito.getCodigo());
            properties.put("electores", circuito.getElectores());
            properties.put("porcentajeVoto", porcentajeVoto);
            properties.put("porcentajeNbi", porcentajeNbi);
            properties.put("hogaresNbiPonderado", redondearDosDecimales(hogaresNbiPonderado));
            properties.put("hogaresPonderados", redondearDosDecimales(hogaresPonderados));
            properties.put("votosDetalle", construirDetalleVotos(votosCircuito, circuito.getElectores()));

            Geometry geometria = circuito.getGeometria();
            if (geometria != null) {
                features.add(GeoJsonHelper.toFeature(geometria, properties));
            }
        }

        // Crear respuesta completa
        Map<String, Object> response = GeoJsonHelper.toFeatureCollection(features);
        
        // Agregar metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("universo", universo != null ? universo : "AFIRMATIVOS");
        metadata.put("opcionVotoId", opcionVotoId);
        metadata.put("departamentoIds", departamentoIds);
        metadata.put("totalCircuitos", features.size());

        response.put("metadata", metadata);
        
        return response;
    }

    private int calcularDenominador(List<CircuitoElectoralOpcionVoto> votos, String universo, Integer electores) {
        if (electores == null) return 0;
        
        return switch (universo) {
            case "AFIRMATIVOS" -> votos.stream()
                .filter(v -> v.getOpcionVoto().getEsAfirmativo())
                .map(CircuitoElectoralOpcionVoto::getCantidad)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
            case "EFECTIVOS" -> votos.stream()
                .map(CircuitoElectoralOpcionVoto::getCantidad)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
            case "HABILITADOS" -> electores;
            default ->electores;
        };
    }

    private Map<Integer, Double[]> calcularNbiPorCircuito(List<CircuitoElectoral> circuitos) {
        Map<Integer, Double[]> result = new HashMap<>();
        
        for (CircuitoElectoral c : circuitos) {
            List<RadioCircuitoIntersec> intersecs = intersecRepository.findByCircuitoElectoralId(c.getId());
            double nbiPonderado = 0;
            double hogaresPonderados = 0;
            
            for (RadioCircuitoIntersec intersec : intersecs) {
                RadioCensal radio = intersec.getRadioCensal();
                double coef = intersec.getCoeficienteArea().doubleValue();
                nbiPonderado += radio.getHogaresNBI() * coef;
                hogaresPonderados += radio.getHogaresTotal() * coef;
            }
            
            result.put(c.getId(), new Double[]{nbiPonderado, hogaresPonderados});
        }
        
        return result;
    }

    private List<Map<String, Object>> construirDetalleVotos(List<CircuitoElectoralOpcionVoto> votosCircuito, Integer electores) {
        int totalEmitidos = votosCircuito.stream()
            .map(CircuitoElectoralOpcionVoto::getCantidad)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .sum();

        int totalElectores = electores != null ? electores : 0;
        int abstenciones = Math.max(totalElectores - totalEmitidos, 0);

        List<Map<String, Object>> detalle = new ArrayList<>();
        for (CircuitoElectoralOpcionVoto voto : votosCircuito) {
            int cantidad = voto.getCantidad() != null ? voto.getCantidad() : 0;
            double porcentaje = totalElectores > 0 ? (cantidad * 100.0) / totalElectores : 0.0;

            Map<String, Object> item = new HashMap<>();
            item.put("id", voto.getOpcionVoto().getId());
            item.put("nombre", voto.getOpcionVoto().getNombre());
            item.put("cantidad", cantidad);
            item.put("porcentaje", redondearDosDecimales(porcentaje));
            detalle.add(item);
        }

        Map<String, Object> abstencion = new HashMap<>();
        abstencion.put("id", -1);
        abstencion.put("nombre", "Abstención");
        abstencion.put("cantidad", abstenciones);
        abstencion.put("porcentaje", redondearDosDecimales(totalElectores > 0 ? (abstenciones * 100.0) / totalElectores : 0.0));
        detalle.add(abstencion);

        return detalle;
    }

    private double redondearDosDecimales(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
