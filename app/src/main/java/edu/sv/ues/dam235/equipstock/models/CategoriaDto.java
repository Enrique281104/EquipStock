package edu.sv.ues.dam235.equipstock.models;

public class CategoriaDto {
    private Long id;
    private String nombre;
    private String descripcion;
    private String icono;
    private long equiposVinculados;

    public CategoriaDto(String nombre, String descripcion, String icono) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.icono = icono;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getIcono() { return icono; }
    public long getEquiposVinculados() { return equiposVinculados; }
}