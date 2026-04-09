package com.psv.datos_mapas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import org.locationtech.jts.geom.Geometry;

@Entity
@Table(name = "departamentos")
@Immutable
public class Departamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_departamento")
    private Integer id;

    @Column(name = "codigo_indec", length = 5)
    private String codigoINDEC;

    @Column(name = "nombre", length = 50, nullable = false)
    private String nombre;

    @Column(name = "geometria", columnDefinition = "geometry(MultiPolygon,4326)")
    private Geometry geometria;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigoINDEC() {
        return codigoINDEC;
    }

    public void setCodigoINDEC(String codigoINDEC) {
        this.codigoINDEC = codigoINDEC;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Geometry getGeometria() {
        return geometria;
    }

    public void setGeometria(Geometry geometria) {
        this.geometria = geometria;
    }
}