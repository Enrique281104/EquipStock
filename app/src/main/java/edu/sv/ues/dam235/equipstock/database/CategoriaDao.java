package edu.sv.ues.dam235.equipstock.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface CategoriaDao {

    @Query("SELECT * FROM categorias WHERE syncStatus <> 'PENDING_DELETE' "
            + "AND nombre LIKE '%' || :texto || '%' ORDER BY nombre COLLATE NOCASE")
    LiveData<List<CategoriaEntity>> buscar(String texto);

    @Query("SELECT COUNT(*) FROM categorias WHERE syncStatus <> 'SYNCED'")
    LiveData<Integer> contarPendientes();

    @Query("SELECT * FROM categorias WHERE syncStatus <> 'PENDING_DELETE' "
            + "ORDER BY nombre COLLATE NOCASE")
    LiveData<List<CategoriaEntity>> todas();

    @Query("SELECT * FROM categorias WHERE localId = :id")
    CategoriaEntity porLocalId(long id);

    @Query("SELECT * FROM categorias WHERE serverId = :id")
    CategoriaEntity porServerId(long id);

    @Query("SELECT * FROM categorias WHERE syncStatus <> 'SYNCED' ORDER BY localId")
    List<CategoriaEntity> pendientes();

    @Query("SELECT * FROM categorias WHERE syncStatus = 'SYNCED' AND serverId IS NOT NULL")
    List<CategoriaEntity> sincronizadas();

    @Insert
    long insertar(CategoriaEntity c);

    @Update
    void actualizar(CategoriaEntity c);

    @Query("DELETE FROM categorias WHERE localId = :id")
    void borrar(long id);

    @Query("UPDATE categorias SET equiposVinculados = (SELECT COUNT(*) FROM equipos "
            + "WHERE equipos.categoriaLocalId = categorias.localId "
            + "AND equipos.syncStatus <> 'PENDING_DELETE')")
    void recalcularConteo();
}