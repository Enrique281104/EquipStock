package edu.sv.ues.dam235.equipstock.database;

import androidx.room.Embedded;

public class EquipoConDetalle {
    @Embedded
    public EquipoEntity equipo;
    public String categoriaNombre;
    public String categoriaIcono;
    public String ubicacionNombre;
}