package com.psv.datos_mapas.dto;

import com.psv.datos_mapas.model.Departamento;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTWriter;

public class DepartamentoSimpleDTO {
    private Integer id;
    private String codigoINDEC;
    private String nombre;
    private String geometriaWKT;

    public DepartamentoSimpleDTO() {}

    public static DepartamentoSimpleDTO fromEntity(Departamento depto) {
        DepartamentoSimpleDTO dto = new DepartamentoSimpleDTO();
        dto.setId(depto.getId());
        dto.setCodigoINDEC(depto.getCodigoINDEC());
        dto.setNombre(depto.getNombre());
        
        Geometry geom = depto.getGeometria();
        if (geom != null) {
            WKTWriter writer = new WKTWriter();
            dto.setGeometriaWKT(writer.write(geom));
        }
        
        return dto;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCodigoINDEC() { return codigoINDEC; }
    public void setCodigoINDEC(String codigoINDEC) { this.codigoINDEC = codigoINDEC; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getGeometriaWKT() { return geometriaWKT; }
    public void setGeometriaWKT(String geometriaWKT) { this.geometriaWKT = geometriaWKT; }
}