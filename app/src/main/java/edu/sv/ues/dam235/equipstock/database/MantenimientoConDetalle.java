package edu.sv.ues.dam235.equipstock.database;

import androidx.room.Embedded;

public class MantenimientoConDetalle {
    @Embedded
    public MantenimientoEntity mantenimiento;
    public String equipoNombre;
    public String equipoSerie;
    public String equipoCodigo;
    public String equipoCategoriaIcono;
    public String equipoUbicacion;
}