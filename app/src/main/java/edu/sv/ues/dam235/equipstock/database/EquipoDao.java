package edu.sv.ues.dam235.equipstock.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface EquipoDao {

    @Query("SELECT e.*, c.nombre AS categoriaNombre, c.icono AS categoriaIcono, "
            + "u.nombre AS ubicacionNombre "
            + "FROM equipos e "
            + "LEFT JOIN categorias c ON c.localId = e.categoriaLocalId "
            + "LEFT JOIN ubicaciones u ON u.localId = e.ubicacionLocalId "
            + "WHERE e.syncStatus <> 'PENDING_DELETE' "
            + "AND (:estado = '' OR e.estado = :estado) "
            + "AND (e.nombre LIKE '%' || :texto || '%' "
            + "  OR e.numeroSerie LIKE '%' || :texto || '%' "
            + "  OR e.codigoActivo LIKE '%' || :texto || '%') "
            + "ORDER BY e.nombre COLLATE NOCASE")
    LiveData<List<EquipoConDetalle>> buscar(String texto, String estado);

    @Query("SELECT e.*, c.nombre AS categoriaNombre, c.icono AS categoriaIcono, "
            + "u.nombre AS ubicacionNombre "
            + "FROM equipos e "
            + "LEFT JOIN categorias c ON c.localId = e.categoriaLocalId "
            + "LEFT JOIN ubicaciones u ON u.localId = e.ubicacionLocalId "
            + "WHERE e.syncStatus <> 'PENDING_DELETE' AND :texto <> '' "
            + "AND (e.codigoActivo = :texto COLLATE NOCASE "
            + "  OR e.numeroSerie = :texto COLLATE NOCASE) LIMIT 1")
    LiveData<EquipoConDetalle> buscarExacto(String texto);

    @Query("SELECT e.*, c.nombre AS categoriaNombre, c.icono AS categoriaIcono, "
            + "u.nombre AS ubicacionNombre "
            + "FROM equipos e "
            + "LEFT JOIN categorias c ON c.localId = e.categoriaLocalId "
            + "LEFT JOIN ubicaciones u ON u.localId = e.ubicacionLocalId "
            + "WHERE e.localId = :localId")
    LiveData<EquipoConDetalle> observarDetalle(long localId);

    @Query("SELECT COUNT(*) FROM equipos WHERE syncStatus <> 'SYNCED'")
    LiveData<Integer> contarPendientes();

    @Query("SELECT COUNT(*) FROM equipos WHERE syncStatus <> 'PENDING_DELETE'")
    LiveData<Integer> contar();

    @Query("SELECT COUNT(*) FROM equipos WHERE codigoActivo = :codigo AND localId <> :excluir "
            + "AND syncStatus <> 'PENDING_DELETE'")
    int contarCodigo(String codigo, long excluir);

    @Query("SELECT COUNT(*) FROM equipos WHERE numeroSerie = :serie AND localId <> :excluir "
            + "AND syncStatus <> 'PENDING_DELETE'")
    int contarSerie(String serie, long excluir);

    @Query("SELECT * FROM equipos WHERE localId = :id")
    EquipoEntity porLocalId(long id);

    @Query("SELECT * FROM equipos WHERE serverId = :id")
    EquipoEntity porServerId(long id);

    @Query("SELECT * FROM equipos WHERE syncStatus <> 'SYNCED' ORDER BY localId")
    List<EquipoEntity> pendientes();

    @Query("SELECT * FROM equipos WHERE syncStatus = 'SYNCED' AND serverId IS NOT NULL")
    List<EquipoEntity> sincronizadas();

    @Insert
    long insertar(EquipoEntity e);

    @Update
    void actualizar(EquipoEntity e);

    @Query("DELETE FROM equipos WHERE localId = :id")
    void borrar(long id);
}