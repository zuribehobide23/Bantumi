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
    private long duracionSegundos;

    public Resultado(String nombreJugador, String fechaHora, int almacenJugador, int almacenComputador, long duracionSegundos) {
        this.nombreJugador = nombreJugador;
        this.fechaHora = fechaHora;
        this.almacenJugador = almacenJugador;
        this.almacenComputador = almacenComputador;
        this.duracionSegundos = duracionSegundos;
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

    public long getDuracionSegundos() {
        return duracionSegundos;
    }

    public void setDuracionSegundos(long duracionSegundos) {
        this.duracionSegundos = duracionSegundos;
    }
}
