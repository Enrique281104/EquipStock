package edu.sv.ues.dam235.equipstock.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {CategoriaEntity.class, UbicacionEntity.class, EquipoEntity.class,
        MantenimientoEntity.class, TecnicoEntity.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract CategoriaDao categoriaDao();
    public abstract UbicacionDao ubicacionDao();
    public abstract EquipoDao equipoDao();
    public abstract MantenimientoDao mantenimientoDao();
    public abstract TecnicoDao tecnicoDao();
    private static volatile AppDatabase instance;

    public static AppDatabase get(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "equipstock.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}