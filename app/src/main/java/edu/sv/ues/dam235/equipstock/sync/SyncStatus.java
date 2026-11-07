package edu.sv.ues.dam235.equipstock.sync;

public final class SyncStatus {
    public static final String SYNCED = "SYNCED";
    public static final String PENDING_CREATE = "PENDING_CREATE";
    public static final String PENDING_UPDATE = "PENDING_UPDATE";
    public static final String PENDING_DELETE = "PENDING_DELETE";

    private SyncStatus() { }
}