package com.psv.datos_mapas.service;

import com.psv.datos_mapas.model.CircuitoElectoral;
import com.psv.datos_mapas.model.CircuitoElectoralOpcionVoto;
import com.psv.datos_mapas.model.RadioCensal;
import com.psv.datos_mapas.model.RadioCircuitoIntersec;
import com.psv.datos_mapas.repository.CircuitoElectoralRepository;
import com.psv.datos_mapas.repository.CircuitoElectoralOpcionVotoRepository;
import com.psv.datos_mapas.repository.RadioCircuitoIntersecRepository;
import com.psv.datos_mapas.util.GeoJsonHelper;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
        List<CircuitoElectoral> circuitos = (departamentoIds == null || departamentoIds.isEmpty())
            ? circuitoRepository.findAll()
            : circuitoRepository.findByDepartamentoIdIn(departamentoIds);

        if (circuitos.isEmpty()) {
            return buildResponse(Collections.emptyList(), universo, opcionVotoId, departamentoIds, new AnalisisResultados());
        }

        List<Integer> circuitoIds = circuitos.stream().map(CircuitoElectoral::getId).toList();
        Map<Integer, List<CircuitoElectoralOpcionVoto>> votosPorCircuito = cargarVotosPorCircuito(circuitoIds);
        Map<Integer, Double[]> nbiPorCircuito = calcularNbiPorCircuito(circuitoIds);

        List<Map<String, Object>> features = new ArrayList<>();
        List<Double> porcentajesVotos = new ArrayList<>();
        List<Double> porcentajesNbi = new ArrayList<>();

        for (CircuitoElectoral circuito : circuitos) {
            Integer circId = circuito.getId();
            List<CircuitoElectoralOpcionVoto> votosCircuito = votosPorCircuito.getOrDefault(circId, Collections.emptyList());

            int denominador = calcularDenominador(votosCircuito, universo, circuito.getElectores());

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

            Double[] nbiData = nbiPorCircuito.get(circId);
            Double hogaresNbiPonderado = (nbiData != null) ? nbiData[0] : 0.0;
            Double hogaresPonderados = (nbiData != null) ? nbiData[1] : 0.0;
            Double porcentajeNbi = (nbiData != null && nbiData[1] > 0) 
                ? (nbiData[0] / nbiData[1]) * 100.0 
                : 0.0;
            porcentajeNbi = redondearDosDecimales(porcentajeNbi);

            if (porcentajeVoto != null) {
                porcentajesVotos.add(porcentajeVoto);
                porcentajesNbi.add(porcentajeNbi);
            }

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

        AnalisisResultados analisis = calcularAnalisis(porcentajesVotos, porcentajesNbi);
        return buildResponse(features, universo, opcionVotoId, departamentoIds, analisis);
    }

    private Map<Integer, List<CircuitoElectoralOpcionVoto>> cargarVotosPorCircuito(List<Integer> circuitoIds) {
        return votoRepository.findByCircuitoElectoralIdIn(circuitoIds)
            .stream()
            .collect(Collectors.groupingBy(v -> v.getCircuitoElectoral().getId()));
    }

    private Map<String, Object> buildResponse(List<Map<String, Object>> features,
                                              String universo,
                                              Integer opcionVotoId,
                                              List<Integer> departamentoIds,
                                              AnalisisResultados analisis) {
        Map<String, Object> response = GeoJsonHelper.toFeatureCollection(features);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("universo", universo != null ? universo : "AFIRMATIVOS");
        metadata.put("opcionVotoId", opcionVotoId);
        metadata.put("departamentoIds", departamentoIds);
        metadata.put("totalCircuitos", features.size());
        metadata.put("correlacion", analisis.correlacion);
        metadata.put("interpretacion", analisis.interpretacion);
        metadata.put("votos", analisis.votos);
        metadata.put("nbi", analisis.nbi);
        metadata.put("regresionPendiente", analisis.regresionPendiente);
        metadata.put("regresionIntercepto", analisis.regresionIntercepto);
        metadata.put("totalPuntosAnalisis", analisis.votos.size());

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

    private Map<Integer, Double[]> calcularNbiPorCircuito(List<Integer> circuitoIds) {
        Map<Integer, Double[]> result = new HashMap<>();
        Map<Integer, List<RadioCircuitoIntersec>> interseccionesPorCircuito = intersecRepository
            .findByCircuitoElectoralIdIn(circuitoIds)
            .stream()
            .collect(Collectors.groupingBy(i -> i.getCircuitoElectoral().getId()));

        for (Integer circuitoId : circuitoIds) {
            List<RadioCircuitoIntersec> intersecs = interseccionesPorCircuito.getOrDefault(circuitoId, Collections.emptyList());
            double nbiPonderado = 0;
            double hogaresPonderados = 0;
            
            for (RadioCircuitoIntersec intersec : intersecs) {
                RadioCensal radio = intersec.getRadioCensal();
                if (radio == null) {
                    continue;
                }
                double coef = intersec.getCoeficienteArea().doubleValue();
                int hogaresNbi = radio.getHogaresNBI() != null ? radio.getHogaresNBI() : 0;
                int hogaresTotal = radio.getHogaresTotal() != null ? radio.getHogaresTotal() : 0;
                nbiPonderado += hogaresNbi * coef;
                hogaresPonderados += hogaresTotal * coef;
            }
            
            result.put(circuitoId, new Double[]{nbiPonderado, hogaresPonderados});
        }
        
        return result;
    }

    private AnalisisResultados calcularAnalisis(List<Double> votos, List<Double> nbi) {
        AnalisisResultados resultado = new AnalisisResultados();
        resultado.votos = votos;
        resultado.nbi = nbi;
        resultado.interpretacion = "Sin datos suficientes";

        if (votos.size() < 2 || votos.size() != nbi.size()) {
            return resultado;
        }

        double[] x = votos.stream().mapToDouble(Double::doubleValue).toArray();
        double[] y = nbi.stream().mapToDouble(Double::doubleValue).toArray();

        double correlacion = new PearsonsCorrelation().correlation(x, y);
        if (!Double.isNaN(correlacion) && !Double.isInfinite(correlacion)) {
            resultado.correlacion = redondearDosDecimales(correlacion);
            resultado.interpretacion = interpretarPearson(resultado.correlacion);
        } else {
            resultado.interpretacion = "Sin variacion suficiente para calcular correlacion";
        }

        RegresionLineal regresion = calcularRegresionLineal(votos, nbi);
        if (regresion != null) {
            resultado.regresionPendiente = redondearDosDecimales(regresion.pendiente);
            resultado.regresionIntercepto = redondearDosDecimales(regresion.intercepto);
        }

        return resultado;
    }

    private RegresionLineal calcularRegresionLineal(List<Double> x, List<Double> y) {
        int n = x.size();
        if (n < 2 || n != y.size()) {
            return null;
        }

        double meanX = x.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double meanY = y.stream().mapToDouble(Double::doubleValue).average().orElse(0);

        double numerador = 0;
        double denominador = 0;

        for (int i = 0; i < n; i++) {
            double dx = x.get(i) - meanX;
            numerador += dx * (y.get(i) - meanY);
            denominador += dx * dx;
        }

        if (denominador == 0) {
            return null;
        }

        RegresionLineal regresion = new RegresionLineal();
        regresion.pendiente = numerador / denominador;
        regresion.intercepto = meanY - (regresion.pendiente * meanX);
        return regresion;
    }

    private String interpretarPearson(double r) {
        double absR = Math.abs(r);
        if (absR >= 0.7) {
            return "Correlación fuerte " + (r > 0 ? "positiva" : "negativa");
        }
        if (absR >= 0.4) {
            return "Correlación moderada " + (r > 0 ? "positiva" : "negativa");
        }
        if (absR >= 0.2) {
            return "Correlación débil";
        }
        return "Correlación nula";
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

    private static class AnalisisResultados {
        private Double correlacion;
        private String interpretacion;
        private List<Double> votos = new ArrayList<>();
        private List<Double> nbi = new ArrayList<>();
        private Double regresionPendiente;
        private Double regresionIntercepto;
    }

    private static class RegresionLineal {
        private double pendiente;
        private double intercepto;
    }
}
