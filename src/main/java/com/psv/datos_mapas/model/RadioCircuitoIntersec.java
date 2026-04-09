package com.psv.datos_mapas.model;

import java.math.BigDecimal;

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

@Entity
@Table(name = "radio_circuito_intersec")
@Immutable
public class RadioCircuitoIntersec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_interseccion")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_radio_censal")
    private RadioCensal radioCensal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_circuito_electoral")
    private CircuitoElectoral circuitoElectoral;

    @Column(name = "coeficiente_area", precision = 10, scale = 8)
    private BigDecimal coeficienteArea;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public RadioCensal getRadioCensal() {
        return radioCensal;
    }

    public void setRadioCensal(RadioCensal radioCensal) {
        this.radioCensal = radioCensal;
    }

    public CircuitoElectoral getCircuitoElectoral() {
        return circuitoElectoral;
    }

    public void setCircuitoElectoral(CircuitoElectoral circuitoElectoral) {
        this.circuitoElectoral = circuitoElectoral;
    }

    public BigDecimal getCoeficienteArea() {
        return coeficienteArea;
    }

    public void setCoeficienteArea(BigDecimal coeficienteArea) {
        this.coeficienteArea = coeficienteArea;
    }
}