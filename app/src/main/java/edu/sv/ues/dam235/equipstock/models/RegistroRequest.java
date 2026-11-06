package edu.sv.ues.dam235.equipstock.models;

public class RegistroRequest {
    private final String nombre;
    private final String email;
    private final String password;
    private final String telefono;

    public RegistroRequest(String nombre, String email, String password, String telefono) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.telefono = telefono;
    }
}