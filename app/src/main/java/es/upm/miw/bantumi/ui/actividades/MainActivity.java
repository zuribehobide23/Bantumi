package es.upm.miw.bantumi.ui.actividades;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.upm.miw.bantumi.datos.BantumiDatabase;
import es.upm.miw.bantumi.datos.Resultado;
import es.upm.miw.bantumi.ui.fragmentos.FinalAlertDialog;
import es.upm.miw.bantumi.ui.fragmentos.RecoverAlertDialog;
import es.upm.miw.bantumi.ui.fragmentos.RestartAlertDialog;
import es.upm.miw.bantumi.R;
import es.upm.miw.bantumi.dominio.logica.JuegoBantumi;
import es.upm.miw.bantumi.ui.viewmodel.BantumiViewModel;

public class MainActivity extends AppCompatActivity {

    protected final String LOG_TAG = "MiW";
    public static final String TAG_MIW = "MiW";
    public JuegoBantumi juegoBantumi;
    private BantumiViewModel bantumiVM;
    int numInicialSemillas;
    private boolean puntuacionGuardada = false;

    public void iniciarNuevaPartida() {
        android.content.SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        String numSemillasStr = preferences.getString("prefKeyNumInicialSemillas", String.valueOf(getResources().getInteger(R.integer.intNumInicialSemillas)));
        try {
            numInicialSemillas = Integer.parseInt(numSemillasStr);
        } catch (Exception e) {
            numInicialSemillas = getResources().getInteger(R.integer.intNumInicialSemillas);
        }
        if (bantumiVM != null) {
            juegoBantumi = new JuegoBantumi(bantumiVM, JuegoBantumi.Turno.turnoJ1, numInicialSemillas);
            juegoBantumi.inicializar(JuegoBantumi.Turno.turnoJ1);
        }
        puntuacionGuardada = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Instancia el ViewModel y el juego, y asigna observadores a los huecos
        bantumiVM = new ViewModelProvider(this).get(BantumiViewModel.class);
        iniciarNuevaPartida();
        crearObservadores();
    }

    @Override
    protected void onResume() {
        super.onResume();
        android.content.SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        String numSemillasStr = preferences.getString("prefKeyNumInicialSemillas", String.valueOf(getResources().getInteger(R.integer.intNumInicialSemillas)));
        int nuevasSemillas;
        try {
            nuevasSemillas = Integer.parseInt(numSemillasStr);
        } catch (Exception e) {
            nuevasSemillas = getResources().getInteger(R.integer.intNumInicialSemillas);
        }
        if (nuevasSemillas != numInicialSemillas) {
            numInicialSemillas = nuevasSemillas;
            iniciarNuevaPartida();
        }
    }

    /**
     * Crea y subscribe los observadores asignados a las posiciones del tablero.
     * Si se modifica el contenido del tablero -> se actualiza la vista.
     */
    private void crearObservadores() {
        for (int i = 0; i < JuegoBantumi.NUM_POSICIONES; i++) {
            int finalI = i;
            bantumiVM.getNumSemillas(i).observe(    // Huecos y almacenes
                    this,
                    new Observer<Integer>() {
                        @Override
                        public void onChanged(Integer integer) {
                            mostrarValor(finalI, juegoBantumi.getSemillas(finalI));
                        }
                    });
        }
        bantumiVM.getTurno().observe(   // Turno
                this,
                new Observer<JuegoBantumi.Turno>() {
                    @Override
                    public void onChanged(JuegoBantumi.Turno turno) {
                        marcarTurno(juegoBantumi.turnoActual());
                    }
                }
        );
    }

    /**
     * Indica el turno actual cambiando el color del texto
     *
     * @param turnoActual turno actual
     */
    private void marcarTurno(@NonNull JuegoBantumi.Turno turnoActual) {
        TextView tvJugador1 = findViewById(R.id.tvPlayer1);
        TextView tvJugador2 = findViewById(R.id.tvPlayer2);
        switch (turnoActual) {
            case turnoJ1:
                tvJugador1.setTextColor(getColor(R.color.white));
                tvJugador1.setBackgroundColor(getColor(android.R.color.holo_blue_light));
                tvJugador2.setTextColor(getColor(R.color.black));
                tvJugador2.setBackgroundColor(getColor(R.color.white));
                break;
            case turnoJ2:
                tvJugador1.setTextColor(getColor(R.color.black));
                tvJugador1.setBackgroundColor(getColor(R.color.white));
                tvJugador2.setTextColor(getColor(R.color.white));
                tvJugador2.setBackgroundColor(getColor(android.R.color.holo_blue_light));
                break;
            default:
                tvJugador1.setTextColor(getColor(R.color.black));
                tvJugador2.setTextColor(getColor(R.color.black));
        }
    }

    /**
     * Muestra el valor <i>valor</i> en la posición <i>pos</i>
     *
     * @param pos posición a actualizar
     * @param valor valor a mostrar
     */
    private void mostrarValor(int pos, int valor) {
        String num2digitos = String.format(Locale.getDefault(), "%02d", pos);
        // Los identificadores de los huecos tienen el formato casilla_XX
        int idBoton = getResources().getIdentifier("casilla_" + num2digitos, "id", getPackageName());
        if (0 != idBoton) {
            TextView viewHueco = findViewById(idBoton);
            viewHueco.setText(String.valueOf(valor));
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.opciones_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.opcAcercaDe) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.aboutTitle)
                    .setMessage(R.string.aboutMessage)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        } else if (itemId == R.id.opcReiniciarPartida) {
            new RestartAlertDialog().show(getSupportFragmentManager(), "RESTART_DIALOG");
        } else if (itemId == R.id.opcRecuperarPartida) {
            recuperarPartida();
        } else if (itemId == R.id.opcMejoresResultados) {
            mostrarMejoresResultados();
        } else if (itemId == R.id.opcAjustes) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else {
            Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.txtSinImplementar),
                    Snackbar.LENGTH_LONG
            ).show();
        }
        return true;
    }

    public void mostrarMejoresResultados() {
        List<Resultado> resultados = BantumiDatabase.getInstance(this).resultadoDao().obtenerMejoresResultados();
        if (resultados == null || resultados.isEmpty()) {
            Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.txtNoResultados),
                    Snackbar.LENGTH_LONG
            ).show();
            return;
        }

        String[] items = new String[resultados.size()];
        for (int i = 0; i < resultados.size(); i++) {
            Resultado r = resultados.get(i);
            items[i] = String.format(Locale.getDefault(), "%d. (%s) -> J1: %d | J2: %d",
                    i + 1, r.getFechaHora(), r.getAlmacenJugador(), r.getAlmacenComputador());
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.txtOpcionMejoresResultados)
                .setItems(items, null)
                .setPositiveButton(android.R.string.ok, null)
                .setNeutralButton(R.string.txtBotonEliminar, (dialog, which) -> {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.txtOpcionMejoresResultados)
                            .setMessage(R.string.txtDialogoEliminarResultados)
                            .setPositiveButton(android.R.string.ok, (d, w) -> {
                                BantumiDatabase.getInstance(this).resultadoDao().eliminarTodo();
                                Snackbar.make(
                                        findViewById(android.R.id.content),
                                        getString(R.string.txtResultadosEliminados),
                                        Snackbar.LENGTH_LONG
                                ).show();
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                })
                .show();
    }

    public void recuperarPartida() {
        new RecoverAlertDialog(() -> {
            Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.txtSinImplementar),
                    Snackbar.LENGTH_LONG
            ).show();
        }).show(getSupportFragmentManager(), "RECOVER_DIALOG");
    }

    /**
     * Acción que se ejecuta al pulsar sobre cualquier hueco
     *
     * @param v Vista pulsada (hueco)
     */
    public void huecoPulsado(@NonNull View v) {
        String resourceName = getResources().getResourceEntryName(v.getId()); // pXY
        int num = Integer.parseInt(resourceName.substring(resourceName.length() - 2));
        Log.i(LOG_TAG, "huecoPulsado(" + resourceName + ") num=" + num);
        switch (juegoBantumi.turnoActual()) {
            case turnoJ1:
                Log.i(LOG_TAG, "* Juega Jugador");
                juegoBantumi.jugar(num);
                break;
            case turnoJ2:
                Log.i(LOG_TAG, "* Juega Computador");
                juegoBantumi.juegaComputador();
                break;
            default:    // JUEGO TERMINADO
                finJuego();
        }

        if (juegoBantumi.juegoTerminado()) {
            finJuego();
        }
    }

    /**
     * El juego ha terminado. Volver a jugar?
     */
    private void finJuego() {
        if (!puntuacionGuardada) {
            puntuacionGuardada = true;
            String nombreJugador = androidx.preference.PreferenceManager
                    .getDefaultSharedPreferences(this)
                    .getString("prefKeyNombreJugador", getString(R.string.txtPlayer1));
            String fechaHora = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(new Date());
            int semillasJ1 = juegoBantumi.getSemillas(6);
            int semillasJ2 = juegoBantumi.getSemillas(13);

            Resultado resultado = new Resultado(
                    nombreJugador, fechaHora, semillasJ1, semillasJ2
            );
            BantumiDatabase.getInstance(this)
                    .resultadoDao()
                    .insertar(resultado);
            Log.i(LOG_TAG, "Puntuación guardada en Room: " + nombreJugador + " - J1:" + semillasJ1 + " J2:" + semillasJ2);
        }

        String texto = (juegoBantumi.getSemillas(6) > 6 * numInicialSemillas)
                ? "Gana Jugador 1"
                : "Gana Jugador 2";
        if (juegoBantumi.getSemillas(6) == 6 * numInicialSemillas) {
            texto = "¡¡¡ EMPATE !!!";
        }

        // @TODO guardar puntuación

        // terminar
        new FinalAlertDialog(texto).show(getSupportFragmentManager(), "ALERT_DIALOG");
    }
}