package edu.sv.ues.dam235.equipstock.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class FechaUtil {

    private FechaUtil() { }

    /** Fecha de hoy como texto yyyy-MM-dd. */
    public static String hoy() {
        return LocalDate.now().toString();
    }

    /** De yyyy-MM-dd a dd/MM/yyyy. */
    public static String mostrar(String iso) {
        if (iso == null || iso.length() != 10) return "-";
        return iso.substring(8, 10) + "/" + iso.substring(5, 7) + "/" + iso.substring(0, 4);
    }

    public static String mostrarFechaHora(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

}