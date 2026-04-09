package com.psv.datos_mapas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "opciones_voto")
@Immutable
public class OpcionVoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_opcion_voto")
    private Integer id;

    @Column(name = "codigo", length = 3)
    private String codigo;

    @Column(name = "nombre", length = 100)
    private String nombre;

    @Column(name = "es_afirmativo")
    private Boolean esAfirmativo;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getEsAfirmativo() {
        return esAfirmativo;
    }

    public void setEsAfirmativo(Boolean esAfirmativo) {
        this.esAfirmativo = esAfirmativo;
    }
}