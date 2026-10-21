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
    List<Resultado> obtenerMejoresResultados();

    @Insert
    void insertar(Resultado resultado);

    @Query("DELETE FROM resultados")
    void eliminarTodo();
}
