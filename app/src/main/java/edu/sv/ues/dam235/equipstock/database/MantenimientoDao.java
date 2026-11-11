package edu.sv.ues.dam235.equipstock.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface MantenimientoDao {

    /**
     * estado: "" (todos), COMPLETADO, EN_PROCESO o VENCIDO.
     * Vencido = En Proceso con fecha anterior a hoy.
     * tecnicoId y equipoLocalId: 0 significa "sin filtro".
     */
    @Query("SELECT m.*, e.nombre AS equipoNombre, e.numeroSerie AS equipoSerie, "
            + "e.codigoActivo AS equipoCodigo, c.icono AS equipoCategoriaIcono, "
            + "u.nombre AS equipoUbicacion "
            + "FROM mantenimientos m "
            + "LEFT JOIN equipos e ON e.localId = m.equipoLocalId "
            + "LEFT JOIN categorias c ON c.localId = e.categoriaLocalId "
            + "LEFT JOIN ubicaciones u ON u.localId = e.ubicacionLocalId "
            + "WHERE (:tecnicoId = 0 OR m.tecnicoId = :tecnicoId) "
            + "AND (:equipoLocalId = 0 OR m.equipoLocalId = :equipoLocalId) "
            + "AND (:estado = '' "
            + "  OR (:estado = 'COMPLETADO' AND m.estado = 'COMPLETADO') "
            + "  OR (:estado = 'EN_PROCESO' AND m.estado = 'EN_PROCESO' "
            + "      AND (m.fechaProgramada IS NULL OR m.fechaProgramada >= :hoy)) "
            + "  OR (:estado = 'VENCIDO' AND m.estado = 'EN_PROCESO' "
            + "      AND m.fechaProgramada < :hoy)) "
            + "AND (:texto = '' OR e.nombre LIKE '%' || :texto || '%' "
            + "  OR e.numeroSerie LIKE '%' || :texto || '%' "
            + "  OR e.codigoActivo LIKE '%' || :texto || '%') "
            + "ORDER BY m.fechaProgramada DESC, m.localId DESC")
    LiveData<List<MantenimientoConDetalle>> buscar(String texto, String estado,
                                                   long tecnicoId, long equipoLocalId,
                                                   String hoy);

    @Query("SELECT m.*, e.nombre AS equipoNombre, e.numeroSerie AS equipoSerie, "
            + "e.codigoActivo AS equipoCodigo, c.icono AS equipoCategoriaIcono, "
            + "u.nombre AS equipoUbicacion "
            + "FROM mantenimientos m "
            + "LEFT JOIN equipos e ON e.localId = m.equipoLocalId "
            + "LEFT JOIN categorias c ON c.localId = e.categoriaLocalId "
            + "LEFT JOIN ubicaciones u ON u.localId = e.ubicacionLocalId "
            + "WHERE m.localId = :localId")
    LiveData<MantenimientoConDetalle> observarDetalle(long localId);

    @Query("SELECT * FROM mantenimientos WHERE equipoLocalId = :equipoLocalId "
            + "ORDER BY fechaProgramada DESC, localId DESC LIMIT :limite")
    LiveData<List<MantenimientoEntity>> ultimosDeEquipo(long equipoLocalId, int limite);

    @Query("SELECT * FROM mantenimientos WHERE localId = :id")
    MantenimientoEntity porLocalId(long id);

    @Query("SELECT * FROM mantenimientos WHERE serverId = :id")
    MantenimientoEntity porServerId(long id);

    @Query("SELECT * FROM mantenimientos WHERE syncStatus <> 'SYNCED' ORDER BY localId")
    List<MantenimientoEntity> pendientes();

    @Query("SELECT * FROM mantenimientos WHERE syncStatus = 'SYNCED' AND serverId IS NOT NULL")
    List<MantenimientoEntity> sincronizados();

    @Insert
    long insertar(MantenimientoEntity m);

    @Update
    void actualizar(MantenimientoEntity m);

    @Query("DELETE FROM mantenimientos WHERE localId = :id")
    void borrar(long id);
}