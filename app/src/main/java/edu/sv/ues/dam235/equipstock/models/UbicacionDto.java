package edu.sv.ues.dam235.equipstock.models;

public class UbicacionDto {
    private Long id;
    private String nombre;
    private String descripcion;
    private long equiposVinculados;

    public UbicacionDto(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public long getEquiposVinculados() { return equiposVinculados; }
}