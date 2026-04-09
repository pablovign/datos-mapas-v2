package com.psv.datos_mapas.dto;

public class OpcionVotoDTO {
    private Integer id;
    private String codigo;
    private String nombre;
    private Boolean esAfirmativo;

    public OpcionVotoDTO() {}

    public OpcionVotoDTO(Integer id, String codigo, String nombre, Boolean esAfirmativo) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.esAfirmativo = esAfirmativo;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Boolean getEsAfirmativo() { return esAfirmativo; }
    public void setEsAfirmativo(Boolean esAfirmativo) { this.esAfirmativo = esAfirmativo; }
}