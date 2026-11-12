package edu.sv.ues.dam235.equipstock.models;

public class Usuario {
    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private String rol;
    private String fotoUrl;
    private Boolean activo;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public String getRol() { return rol; }
    public String getFotoUrl() { return fotoUrl; }
    public Boolean getActivo() { return activo; }

    private Long fotoActualizada;

    public Long getFotoActualizada() { return fotoActualizada; }
}
