package edu.sv.ues.dam235.equipstock.database;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "ubicaciones", indices = {@Index(value = {"serverId"}, unique = true)})
public class UbicacionEntity {
    @PrimaryKey(autoGenerate = true)
    public long localId;
    public Long serverId;
    public String nombre;
    public String descripcion;
    public int equiposVinculados;
    public String syncStatus;
    public int version;
}