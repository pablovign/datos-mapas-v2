package com.psv.datos_mapas.dto;

public class DepartamentoDTO {
    private Integer id;
    private String codigoINDEC;
    private String nombre;

    public DepartamentoDTO() {}

    public DepartamentoDTO(Integer id, String codigoINDEC, String nombre) {
        this.id = id;
        this.codigoINDEC = codigoINDEC;
        this.nombre = nombre;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCodigoINDEC() { return codigoINDEC; }
    public void setCodigoINDEC(String codigoINDEC) { this.codigoINDEC = codigoINDEC; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}