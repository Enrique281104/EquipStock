package edu.sv.ues.dam235.equipstock.models;

public class PasswordRequest {
    private final String actual;
    private final String nueva;

    public PasswordRequest(String actual, String nueva) {
        this.actual = actual;
        this.nueva = nueva;
    }
}