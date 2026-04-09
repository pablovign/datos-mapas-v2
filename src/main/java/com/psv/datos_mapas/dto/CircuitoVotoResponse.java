package com.psv.datos_mapas.dto;

import java.util.List;
import java.util.Map;

public class CircuitoVotoResponse {
    private String type;
    private List<Map<String, Object>> features;
    private Metadata metadata;

    public static class Metadata {
        private String universo;
        private Integer opcionVotoId;
        private List<Integer> departamentoIds;
        private Integer totalCircuitos;
        private Double correlacion;
        private String interpretacion;
        private List<Double> votos;
        private List<Double> nbi;

        public String getUniverso() { return universo; }
        public void setUniverso(String universo) { this.universo = universo; }
        public Integer getOpcionVotoId() { return opcionVotoId; }
        public void setOpcionVotoId(Integer opcionVotoId) { this.opcionVotoId = opcionVotoId; }
        public List<Integer> getDepartamentoIds() { return departamentoIds; }
        public void setDepartamentoIds(List<Integer> departamentoIds) { this.departamentoIds = departamentoIds; }
        public Integer getTotalCircuitos() { return totalCircuitos; }
        public void setTotalCircuitos(Integer totalCircuitos) { this.totalCircuitos = totalCircuitos; }
        public Double getCorrelacion() { return correlacion; }
        public void setCorrelacion(Double correlacion) { this.correlacion = correlacion; }
        public String getInterpretacion() { return interpretacion; }
        public void setInterpretacion(String interpretacion) { this.interpretacion = interpretacion; }
        public List<Double> getVotos() { return votos; }
        public void setVotos(List<Double> votos) { this.votos = votos; }
        public List<Double> getNbi() { return nbi; }
        public void setNbi(List<Double> nbi) { this.nbi = nbi; }
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public List<Map<String, Object>> getFeatures() { return features; }
    public void setFeatures(List<Map<String, Object>> features) { this.features = features; }
    public Metadata getMetadata() { return metadata; }
    public void setMetadata(Metadata metadata) { this.metadata = metadata; }
}