package edu.sv.ues.dam235.equipstock.database;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "categorias", indices = {@Index(value = {"serverId"}, unique = true)})
public class CategoriaEntity {
    @PrimaryKey(autoGenerate = true)
    public long localId;      // id propio del teléfono
    public Long serverId;     // id en el servidor (null hasta que se sincroniza)
    public String nombre;
    public String descripcion;
    public String icono;
    public int equiposVinculados;
    public String syncStatus;
    public int version;       // sube con cada edición local
}