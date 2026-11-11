package edu.sv.ues.dam235.equipstock.models;

public class EquipoDto {
    private Long id;
    private String nombre;
    private String numeroSerie;
    private String codigoActivo;
    private String estado;
    private String imagenUrl;
    private Long categoriaId;
    private String categoriaNombre;
    private Long ubicacionId;
    private String ubicacionNombre;

    public EquipoDto(String nombre, String numeroSerie, String codigoActivo, String estado,
                     Long categoriaId, Long ubicacionId) {
        this.nombre = nombre;
        this.numeroSerie = numeroSerie;
        this.codigoActivo = codigoActivo;
        this.estado = estado;
        this.categoriaId = categoriaId;
        this.ubicacionId = ubicacionId;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getNumeroSerie() { return numeroSerie; }
    public String getCodigoActivo() { return codigoActivo; }
    public String getEstado() { return estado; }
    public Long getCategoriaId() { return categoriaId; }
    public Long getUbicacionId() { return ubicacionId; }
}