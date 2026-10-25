package es.upm.miw.bantumi.datos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ResultadoDao {
    @Query("SELECT * FROM resultados ORDER BY id DESC")
    LiveData<List<Resultado>> obtenerResultados();

    @Query("SELECT * FROM resultados ORDER BY MAX(almacenJugador, almacenComputador) DESC LIMIT 10")
    List<Resultado> obtenerMejoresResultadosDesc();

    @Query("SELECT * FROM resultados ORDER BY MAX(almacenJugador, almacenComputador) ASC LIMIT 10")
    List<Resultado> obtenerMejoresResultadosAsc();

    @Query("SELECT * FROM resultados WHERE LOWER(nombreJugador) = LOWER(:nombre) AND almacenJugador > almacenComputador ORDER BY almacenJugador DESC LIMIT 10")
    List<Resultado> obtenerPartidasGanadasPorJugadorDesc(String nombre);

    @Query("SELECT * FROM resultados WHERE LOWER(nombreJugador) = LOWER(:nombre) AND almacenJugador > almacenComputador ORDER BY almacenJugador ASC LIMIT 10")
    List<Resultado> obtenerPartidasGanadasPorJugadorAsc(String nombre);

    @Query("SELECT * FROM resultados WHERE almacenComputador > almacenJugador ORDER BY almacenComputador DESC LIMIT 10")
    List<Resultado> obtenerPartidasGanadasPorComputadorDesc();

    @Query("SELECT * FROM resultados WHERE almacenComputador > almacenJugador ORDER BY almacenComputador ASC LIMIT 10")
    List<Resultado> obtenerPartidasGanadasPorComputadorAsc();

    @Query("SELECT DISTINCT nombreJugador FROM resultados WHERE nombreJugador IS NOT NULL AND nombreJugador != '' ORDER BY nombreJugador ASC")
    List<String> obtenerJugadoresDisponibles();

    @Insert
    void insertar(Resultado resultado);

    @Query("DELETE FROM resultados")
    void eliminarTodo();
}
