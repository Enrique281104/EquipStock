package edu.sv.ues.dam235.equipstock.sync;

import java.io.IOException;

public interface Sincronizable {
    /** Envía los pendientes y trae lo del servidor. Devuelve un mensaje de error o null. */
    String sincronizarAhora() throws IOException;
}