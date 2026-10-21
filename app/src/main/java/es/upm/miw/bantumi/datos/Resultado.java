package es.upm.miw.bantumi.datos;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "resultados")
public class Resultado {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String nombreJugador;
    private String fechaHora;
    private int almacenJugador;
    private int almacenComputador;

    public Resultado(String nombreJugador, String fechaHora, int almacenJugador, int almacenComputador) {
        this.nombreJugador = nombreJugador;
        this.fechaHora = fechaHora;
        this.almacenJugador = almacenJugador;
        this.almacenComputador = almacenComputador;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public int getAlmacenJugador() {
        return almacenJugador;
    }

    public int getAlmacenComputador() {
        return almacenComputador;
    }
}
