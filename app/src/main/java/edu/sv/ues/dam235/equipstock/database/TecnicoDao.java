package edu.sv.ues.dam235.equipstock.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface TecnicoDao {

    @Query("SELECT * FROM tecnicos ORDER BY nombre COLLATE NOCASE")
    LiveData<List<TecnicoEntity>> todos();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertar(List<TecnicoEntity> tecnicos);

    @Query("DELETE FROM tecnicos")
    void borrarTodos();
}