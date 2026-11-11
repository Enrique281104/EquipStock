package edu.sv.ues.dam235.equipstock.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tecnicos")
public class TecnicoEntity {
    @PrimaryKey
    public long id;        // id del usuario en el servidor
    public String nombre;
}