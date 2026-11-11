package edu.sv.ues.dam235.equipstock.database;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "equipos", indices = {@Index(value = {"serverId"}, unique = true)})
public class EquipoEntity {
    @PrimaryKey(autoGenerate = true)
    public long localId;
    public Long serverId;
    public String nombre;
    public String numeroSerie;
    public String codigoActivo;
    public String estado;            // OPERATIVO, EN_MANTENIMIENTO, INACTIVO
    public Long categoriaLocalId;    // referencia por id LOCAL
    public Long ubicacionLocalId;    // referencia por id LOCAL
    public String syncStatus;
    public int version;
}