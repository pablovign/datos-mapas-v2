package com.psv.datos_mapas.dto;

import com.psv.datos_mapas.model.Departamento;
import org.locationtech.jts.geom.Geometry;

public class DepartamentoConGeometriaDTO {
    private Integer id;
    private String codigoINDEC;
    private String nombre;
    private Geometry geometria;

    public DepartamentoConGeometriaDTO() {}

    public static DepartamentoConGeometriaDTO fromEntity(Departamento depto) {
        DepartamentoConGeometriaDTO dto = new DepartamentoConGeometriaDTO();
        dto.setId(depto.getId());
        dto.setCodigoINDEC(depto.getCodigoINDEC());
        dto.setNombre(depto.getNombre());
        dto.setGeometria(depto.getGeometria());
        return dto;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCodigoINDEC() { return codigoINDEC; }
    public void setCodigoINDEC(String codigoINDEC) { this.codigoINDEC = codigoINDEC; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Geometry getGeometria() { return geometria; }
    public void setGeometria(Geometry geometria) { this.geometria = geometria; }
}