package edu.sv.ues.dam235.equipstock.database;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "mantenimientos", indices = {@Index(value = {"serverId"}, unique = true)})
public class MantenimientoEntity {
    @PrimaryKey(autoGenerate = true)
    public long localId;
    public Long serverId;
    public Long equipoLocalId;     // referencia por id LOCAL
    public Long tecnicoId;         // id del usuario en el servidor
    public String tecnicoNombre;
    public String tipo;            // PREVENTIVO, CORRECTIVO, INCIDENCIA
    public String estado;          // COMPLETADO, EN_PROCESO
    public String fechaProgramada; // "yyyy-MM-dd"
    public String descripcion;
    public String observaciones;
    public String syncStatus;
    public int version;
}