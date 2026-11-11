package edu.sv.ues.dam235.equipstock.models;

public class MantenimientoDto {
    private Long id;
    private Long equipoId;
    private String equipoNombre;
    private Long tecnicoId;
    private String tecnicoNombre;
    private String tipo;
    private String estado;
    private String fechaProgramada;
    private String descripcion;
    private String observaciones;

    public MantenimientoDto(Long equipoId, Long tecnicoId, String tipo, String estado,
                            String fechaProgramada, String descripcion, String observaciones) {
        this.equipoId = equipoId;
        this.tecnicoId = tecnicoId;
        this.tipo = tipo;
        this.estado = estado;
        this.fechaProgramada = fechaProgramada;
        this.descripcion = descripcion;
        this.observaciones = observaciones;
    }

    public Long getId() { return id; }
    public Long getEquipoId() { return equipoId; }
    public Long getTecnicoId() { return tecnicoId; }
    public String getTecnicoNombre() { return tecnicoNombre; }
    public String getTipo() { return tipo; }
    public String getEstado() { return estado; }
    public String getFechaProgramada() { return fechaProgramada; }
    public String getDescripcion() { return descripcion; }
    public String getObservaciones() { return observaciones; }
}