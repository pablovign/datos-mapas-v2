package com.psv.datos_mapas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import org.locationtech.jts.geom.Geometry;

@Entity
@Table(name = "radios_censales")
@Immutable
public class RadioCensal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_radio_censal")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_departamento")
    private Departamento departamento;

    @Column(name = "codigo_indec", length = 9)
    private String codigoINDEC;

    @Column(name = "hogares_total")
    private Integer hogaresTotal;

    @Column(name = "hogares_nbi")
    private Integer hogaresNBI;

    @Column(name = "geometria", columnDefinition = "geometry(MultiPolygon,4326)")
    private Geometry geometria;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
    }

    public String getCodigoINDEC() {
        return codigoINDEC;
    }

    public void setCodigoINDEC(String codigoINDEC) {
        this.codigoINDEC = codigoINDEC;
    }

    public Integer getHogaresTotal() {
        return hogaresTotal;
    }

    public void setHogaresTotal(Integer hogaresTotal) {
        this.hogaresTotal = hogaresTotal;
    }

    public Integer getHogaresNBI() {
        return hogaresNBI;
    }

    public void setHogaresNBI(Integer hogaresNBI) {
        this.hogaresNBI = hogaresNBI;
    }

    public Geometry getGeometria() {
        return geometria;
    }

    public void setGeometria(Geometry geometria) {
        this.geometria = geometria;
    }
}