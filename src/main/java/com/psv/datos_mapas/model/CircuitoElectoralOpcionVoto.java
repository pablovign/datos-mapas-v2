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

@Entity
@Table(name = "circuito_electoral_opcion_voto")
@Immutable
public class CircuitoElectoralOpcionVoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_circuito_electoral_opcion_voto")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_circuito_electoral")
    private CircuitoElectoral circuitoElectoral;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_opcion_voto")
    private OpcionVoto opcionVoto;

    @Column(name = "cantidad")
    private Integer cantidad;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CircuitoElectoral getCircuitoElectoral() {
        return circuitoElectoral;
    }

    public void setCircuitoElectoral(CircuitoElectoral circuitoElectoral) {
        this.circuitoElectoral = circuitoElectoral;
    }

    public OpcionVoto getOpcionVoto() {
        return opcionVoto;
    }

    public void setOpcionVoto(OpcionVoto opcionVoto) {
        this.opcionVoto = opcionVoto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}