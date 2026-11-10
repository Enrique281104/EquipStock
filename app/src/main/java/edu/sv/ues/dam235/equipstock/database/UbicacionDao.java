package edu.sv.ues.dam235.equipstock.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface UbicacionDao {

    @Query("SELECT * FROM ubicaciones WHERE syncStatus <> 'PENDING_DELETE' "
            + "AND nombre LIKE '%' || :texto || '%' ORDER BY nombre COLLATE NOCASE")
    LiveData<List<UbicacionEntity>> buscar(String texto);

    @Query("SELECT * FROM ubicaciones WHERE localId = :id")
    UbicacionEntity porLocalId(long id);

    @Query("SELECT * FROM ubicaciones WHERE serverId = :id")
    UbicacionEntity porServerId(long id);

    @Query("SELECT * FROM ubicaciones WHERE syncStatus <> 'SYNCED' ORDER BY localId")
    List<UbicacionEntity> pendientes();

    @Query("SELECT * FROM ubicaciones WHERE syncStatus = 'SYNCED' AND serverId IS NOT NULL")
    List<UbicacionEntity> sincronizadas();

    @Insert
    long insertar(UbicacionEntity u);

    @Update
    void actualizar(UbicacionEntity u);

    @Query("DELETE FROM ubicaciones WHERE localId = :id")
    void borrar(long id);
}