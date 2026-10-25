package es.upm.miw.bantumi.ui.actividades;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
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
import es.upm.miw.bantumi.datos.PartidaGuardada;
import es.upm.miw.bantumi.datos.Resultado;
import es.upm.miw.bantumi.datos.ResultadoDao;
import es.upm.miw.bantumi.ui.fragmentos.FinalAlertDialog;
import es.upm.miw.bantumi.ui.fragmentos.RestartAlertDialog;
import es.upm.miw.bantumi.R;
import es.upm.miw.bantumi.dominio.logica.JuegoBantumi;
import es.upm.miw.bantumi.ui.viewmodel.BantumiViewModel;

public class MainActivity extends AppCompatActivity {

    protected final String LOG_TAG = "MiW";
    public JuegoBantumi juegoBantumi;
    private BantumiViewModel bantumiVM;
    private Chronometer cronometro;
    private boolean partidaIniciada = false;
    int numInicialSemillas;
    private boolean puntuacionGuardada = false;
    private String lastNumSemillas = "";
    private String lastTurnoInicial = "";
    private String lastNombreJugador = "";
    private String lastColorFondo = "";
    private String lastColorCasillas = "";
    private String lastColorSemillas = "";
    private String lastColorNombre = "";
    private String lastColorIconos = "";
    private String lastColorTurnoFondo = "";
    private String temaActual = "azul";
    private boolean esOrdenAscendente = false;
    private String jugadorFiltro = null;
    private int ultimaCasillaResaltada = -1;

    private void iniciarCronometro() {
        if (!partidaIniciada) {
            partidaIniciada = true;
            if (cronometro != null) {
                cronometro.setBase(SystemClock.elapsedRealtime());
                cronometro.start();
            }
        }
    }

    private void resaltarCasilla(int pos) {
        ultimaCasillaResaltada = pos;
        String num2digitos = String.format(Locale.getDefault(), "%02d", pos);
        int idBoton = getResources().getIdentifier("casilla_" + num2digitos, "id", getPackageName());
        if (0 != idBoton) {
            View viewHueco = findViewById(idBoton);
            if (viewHueco != null) {
                android.content.res.ColorStateList originalTint = viewHueco.getBackgroundTintList();
                viewHueco.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(android.R.color.holo_orange_light)));
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    if (viewHueco.getId() == idBoton) {
                        viewHueco.setBackgroundTintList(originalTint);
                        if (ultimaCasillaResaltada == pos) {
                            ultimaCasillaResaltada = -1;
                        }
                    }
                }, 1000); // 1 segundo exacto
            }
        }
    }

    public void iniciarNuevaPartida() {
        android.content.SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        String numSemillasStr = preferences.getString("prefKeyNumInicialSemillas", String.valueOf(getResources().getInteger(R.integer.intNumInicialSemillas)));
        Log.i(LOG_TAG, "iniciarNuevaPartida() -> prefKeyNumInicialSemillas: " + numSemillasStr);
        try {
            numInicialSemillas = Integer.parseInt(numSemillasStr);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error parsing numInicialSemillas", e);
            numInicialSemillas = getResources().getInteger(R.integer.intNumInicialSemillas);
        }

        String turnoStr = preferences.getString("prefKeyTurnoInicial", "turnoJ1");
        Log.i(LOG_TAG, "iniciarNuevaPartida() -> prefKeyTurnoInicial: " + turnoStr);
        JuegoBantumi.Turno turnoInicial;
        if ("turnoAleatorio".equals(turnoStr)) {
            turnoInicial = (Math.random() < 0.5) ? JuegoBantumi.Turno.turnoJ1 : JuegoBantumi.Turno.turnoJ2;
        } else {
            try {
                turnoInicial = JuegoBantumi.Turno.valueOf(turnoStr);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error parsing turnoInicial", e);
                turnoInicial = JuegoBantumi.Turno.turnoJ1;
            }
        }

        String nombreJugador = preferences.getString("prefKeyNombreJugador", getString(R.string.txtPlayer1));
        TextView tvPlayer1 = findViewById(R.id.tvPlayer1);
        if (tvPlayer1 != null) {
            tvPlayer1.setText(nombreJugador);
        }

        lastNumSemillas = numSemillasStr;
        lastTurnoInicial = turnoStr;
        lastNombreJugador = nombreJugador;

        if (bantumiVM != null) {
            juegoBantumi = new JuegoBantumi(bantumiVM, turnoInicial, numInicialSemillas);
            juegoBantumi.inicializar(turnoInicial);
            juegoBantumi.setJugadaListener(this::resaltarCasilla);
        }

        partidaIniciada = false;
        ultimaCasillaResaltada = -1;
        if (cronometro != null) {
            cronometro.stop();
            cronometro.setBase(SystemClock.elapsedRealtime());
        }
        puntuacionGuardada = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        android.content.SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        temaActual = preferences.getString("prefKeyColorTema", "azul");
        switch (temaActual) {
            case "verde":
                setTheme(R.style.Theme_Bantumi_Green);
                break;
            case "naranja":
                setTheme(R.style.Theme_Bantumi_Orange);
                break;
            case "morado":
                setTheme(R.style.Theme_Bantumi_Purple);
                break;
            default:
                setTheme(R.style.Theme_Bantumi_Blue);
                break;
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cronometro = findViewById(R.id.cronometro);
        bantumiVM = new ViewModelProvider(this).get(BantumiViewModel.class);

        String numSemillasStr = preferences.getString("prefKeyNumInicialSemillas", String.valueOf(getResources().getInteger(R.integer.intNumInicialSemillas)));
        try {
            numInicialSemillas = Integer.parseInt(numSemillasStr);
        } catch (Exception e) {
            numInicialSemillas = getResources().getInteger(R.integer.intNumInicialSemillas);
        }

        String nombreJugador = preferences.getString("prefKeyNombreJugador", getString(R.string.txtPlayer1));
        TextView tvPlayer1 = findViewById(R.id.tvPlayer1);
        if (tvPlayer1 != null) {
            tvPlayer1.setText(nombreJugador);
        }

        lastNumSemillas = numSemillasStr;
        lastTurnoInicial = preferences.getString("prefKeyTurnoInicial", "turnoJ1");
        lastNombreJugador = nombreJugador;

        juegoBantumi = new JuegoBantumi(bantumiVM, bantumiVM.getTurno().getValue() != null ? bantumiVM.getTurno().getValue() : JuegoBantumi.Turno.turnoJ1, numInicialSemillas);
        juegoBantumi.setJugadaListener(this::resaltarCasilla);

        if (savedInstanceState != null) {
            partidaIniciada = savedInstanceState.getBoolean("partidaIniciada", false);
            long base = savedInstanceState.getLong("cronometroBase", 0);
            if (partidaIniciada && cronometro != null && base != 0) {
                cronometro.setBase(base);
                cronometro.start();
            } else if (cronometro != null) {
                cronometro.setBase(SystemClock.elapsedRealtime());
            }
            ultimaCasillaResaltada = savedInstanceState.getInt("ultimaCasillaResaltada", -1);
            if (ultimaCasillaResaltada >= 0) {
                final int posToHighlight = ultimaCasillaResaltada;
                getWindow().getDecorView().post(() -> resaltarCasilla(posToHighlight));
            }
        } else {
            iniciarNuevaPartida();
        }
        crearObservadores();
        aplicarColoresPersonalizados();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("partidaIniciada", partidaIniciada);
        if (cronometro != null) {
            outState.putLong("cronometroBase", cronometro.getBase());
        }
        outState.putInt("ultimaCasillaResaltada", ultimaCasillaResaltada);
    }

    @Override
    protected void onResume() {
        super.onResume();
        android.content.SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        String numSemillasStr = preferences.getString("prefKeyNumInicialSemillas", String.valueOf(getResources().getInteger(R.integer.intNumInicialSemillas)));
        String turnoStr = preferences.getString("prefKeyTurnoInicial", "turnoJ1");
        String nombreJugador = preferences.getString("prefKeyNombreJugador", getString(R.string.txtPlayer1));
        String temaStr = preferences.getString("prefKeyColorTema", "azul");

        String cFondo = preferences.getString("prefKeyColorFondoJuego", "blanco");
        String cCasillas = preferences.getString("prefKeyColorCasillas", "gris");
        String cSemillas = preferences.getString("prefKeyColorSemillas", "negro");
        String cNombre = preferences.getString("prefKeyColorNombreJugador", "negro");
        String cIconos = preferences.getString("prefKeyColorIconos", "azul");
        String cTurnoFondo = preferences.getString("prefKeyColorTurnoFondo", "azul");

        TextView tvPlayer1 = findViewById(R.id.tvPlayer1);
        if (tvPlayer1 != null) {
            tvPlayer1.setText(nombreJugador);
        }

        if (!temaStr.equals(temaActual)) {
            recreate();
            return;
        }

        if (!cFondo.equals(lastColorFondo) || !cCasillas.equals(lastColorCasillas) ||
                !cSemillas.equals(lastColorSemillas) || !cNombre.equals(lastColorNombre) ||
                !cIconos.equals(lastColorIconos) || !cTurnoFondo.equals(lastColorTurnoFondo)) {
            aplicarColoresPersonalizados();
            marcarTurno(juegoBantumi.turnoActual());
        }

        if (!numSemillasStr.equals(lastNumSemillas) || !turnoStr.equals(lastTurnoInicial) || !nombreJugador.equals(lastNombreJugador)) {
            iniciarNuevaPartida();
        }
        aplicarColoresPersonalizados();

        invalidateOptionsMenu();
    }

    private void crearObservadores() {
        for (int i = 0; i < JuegoBantumi.NUM_POSICIONES; i++) {
            int finalI = i;
            bantumiVM.getNumSemillas(i).observe(
                    this,
                    new Observer<Integer>() {
                        @Override
                        public void onChanged(Integer integer) {
                            mostrarValor(finalI, juegoBantumi.getSemillas(finalI));
                        }
                    });
        }
        bantumiVM.getTurno().observe(
                this,
                new Observer<JuegoBantumi.Turno>() {
                    @Override
                    public void onChanged(JuegoBantumi.Turno turno) {
                        marcarTurno(juegoBantumi.turnoActual());
                    }
                }
        );
    }

    private void marcarTurno(@NonNull JuegoBantumi.Turno turnoActual) {
        TextView tvJugador1 = findViewById(R.id.tvPlayer1);
        TextView tvJugador2 = findViewById(R.id.tvPlayer2);
        android.content.SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        String cTurnoFondo = prefs.getString("prefKeyColorTurnoFondo", "azul");
        int colorTurnoBg = obtenerColor(cTurnoFondo, getColor(android.R.color.holo_blue_light));

        switch (turnoActual) {
            case turnoJ1:
                tvJugador1.setTextColor(getColor(R.color.white));
                tvJugador1.setBackgroundColor(colorTurnoBg);
                tvJugador2.setTextColor(getColor(android.R.color.black));
                tvJugador2.setBackgroundColor(getColor(android.R.color.transparent));
                break;
            case turnoJ2:
                tvJugador1.setTextColor(getColor(R.color.black));
                tvJugador1.setBackgroundColor(getColor(android.R.color.transparent));
                tvJugador2.setTextColor(getColor(R.color.white));
                tvJugador2.setBackgroundColor(colorTurnoBg);
                break;
            default:
                tvJugador1.setTextColor(getColor(R.color.black));
                tvJugador2.setTextColor(getColor(R.color.black));
        }
    }

    private int obtenerColor(String valorColor, int defColor) {
        if (valorColor == null || valorColor.trim().isEmpty()) return defColor;
        try {
            if (valorColor.startsWith("#")) {
                return android.graphics.Color.parseColor(valorColor);
            }
        } catch (Exception ignored) {}
        switch (valorColor) {
            case "blanco": return getColor(android.R.color.white);
            case "negro": return getColor(android.R.color.black);
            case "rojo": return getColor(android.R.color.holo_red_dark);
            case "verde": return getColor(android.R.color.holo_green_dark);
            case "azul": return getColor(android.R.color.holo_blue_dark);
            case "naranja": return getColor(android.R.color.holo_orange_dark);
            case "amarillo": return getColor(android.R.color.holo_orange_light);
            case "morado": return getColor(android.R.color.holo_purple);
            case "gris": return getColor(android.R.color.darker_gray);
            default: return defColor;
        }
    }

    private void aplicarColoresPersonalizados() {
        android.content.SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);

        String cFondo = prefs.getString("prefKeyColorFondoJuego", "blanco");
        String cTurnoFondo = prefs.getString("prefKeyColorTurnoFondo", "azul");
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            mainView.setBackgroundColor(obtenerColor(cFondo, getColor(android.R.color.white)));
        }

        String cNombre = prefs.getString("prefKeyColorNombreJugador", "negro");
        int colorNombre = obtenerColor(cNombre, getColor(android.R.color.black));
        TextView tv1 = findViewById(R.id.tvPlayer1);
        TextView tv2 = findViewById(R.id.tvPlayer2);
        if (tv1 != null) tv1.setTextColor(colorNombre);
        if (tv2 != null) tv2.setTextColor(colorNombre);

        String cCasillas = prefs.getString("prefKeyColorCasillas", "gris");
        String cSemillas = prefs.getString("prefKeyColorSemillas", "negro");
        int colorCasilla = obtenerColor(cCasillas, getColor(android.R.color.darker_gray));
        int colorTextoSemilla = obtenerColor(cSemillas, getColor(android.R.color.black));

        for (int i = 0; i < JuegoBantumi.NUM_POSICIONES; i++) {
            String num2digitos = String.format(Locale.getDefault(), "%02d", i);
            int idBoton = getResources().getIdentifier("casilla_" + num2digitos, "id", getPackageName());
            if (idBoton != 0) {
                View viewHueco = findViewById(idBoton);
                if (viewHueco != null) {
                    viewHueco.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorCasilla));
                    if (viewHueco instanceof TextView) {
                        ((TextView) viewHueco).setTextColor(colorTextoSemilla);
                    }
                }
            }
        }

        lastColorFondo = cFondo;
        lastColorCasillas = cCasillas;
        lastColorSemillas = cSemillas;
        lastColorNombre = cNombre;
        lastColorIconos = prefs.getString("prefKeyColorIconos", "azul");
        lastColorTurnoFondo = cTurnoFondo;
    }

    private void mostrarValor(int pos, int valor) {
        String num2digitos = String.format(Locale.getDefault(), "%02d", pos);
        int idBoton = getResources().getIdentifier("casilla_" + num2digitos, "id", getPackageName());
        if (0 != idBoton) {
            TextView viewHueco = findViewById(idBoton);
            viewHueco.setText(String.valueOf(valor));
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.opciones_menu, menu);
        android.content.SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        String cIconos = prefs.getString("prefKeyColorIconos", "azul");
        int colorIcono = obtenerColor(cIconos, getColor(android.R.color.holo_blue_dark));
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.getIcon() != null) {
                item.getIcon().setTint(colorIcono);
            }
        }
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
        } else if (itemId == R.id.opcGuardarPartida) {
            guardarPartida();
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
        mostrarMejoresResultadosConFiltro();
    }

    private void mostrarMejoresResultadosConFiltro() {
        ResultadoDao dao = BantumiDatabase.getInstance(this).resultadoDao();
        List<Resultado> resultados;
        String titulo;
        String nombreJ2 = getString(R.string.txtPlayer2);

        if (jugadorFiltro != null) {
            titulo = String.format(Locale.getDefault(), getString(R.string.texto_ganadas_por_formato), jugadorFiltro);
            if (jugadorFiltro.equalsIgnoreCase(nombreJ2)) {
                if (esOrdenAscendente) {
                    resultados = dao.obtenerPartidasGanadasPorComputadorAsc();
                } else {
                    resultados = dao.obtenerPartidasGanadasPorComputadorDesc();
                }
            } else {
                if (esOrdenAscendente) {
                    resultados = dao.obtenerPartidasGanadasPorJugadorAsc(jugadorFiltro);
                } else {
                    resultados = dao.obtenerPartidasGanadasPorJugadorDesc(jugadorFiltro);
                }
            }
        } else {
            titulo = getString(R.string.filtro_semillas_ganador);
            if (esOrdenAscendente) {
                resultados = dao.obtenerMejoresResultadosAsc();
            } else {
                resultados = dao.obtenerMejoresResultadosDesc();
            }
        }

        String flechaOrden = esOrdenAscendente ? " ⬆" : " ⬇";

        if (resultados == null || resultados.isEmpty()) {
            Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.txtNoResultados) + " (" + titulo + ")",
                    Snackbar.LENGTH_LONG
            ).show();
            if (jugadorFiltro != null) {
                jugadorFiltro = null;
            }
            return;
        }

        CharSequence[] items = new CharSequence[resultados.size()];

        for (int i = 0; i < resultados.size(); i++) {
            Resultado r = resultados.get(i);
            long seg = r.getDuracionSegundos();
            String duracion = String.format(Locale.getDefault(), "%02d:%02d", seg / 60, seg % 60);
            String nameJ1 = (r.getNombreJugador() != null && !r.getNombreJugador().trim().isEmpty())
                    ? r.getNombreJugador()
                    : getString(R.string.txtPlayer1);
            int scoreJ1 = r.getAlmacenJugador();
            int scoreJ2 = r.getAlmacenComputador();

            String htmlText;
            if (scoreJ1 > scoreJ2) {
                htmlText = String.format(Locale.getDefault(),
                        "%d. 🏆 <b><font color='#1B5E20'>%s (%d)</font></b> - %s (%d) | ⏱ %s (%s)",
                        i + 1, nameJ1, scoreJ1, nombreJ2, scoreJ2, duracion, r.getFechaHora());
            } else if (scoreJ2 > scoreJ1) {
                htmlText = String.format(Locale.getDefault(),
                        "%d. %s (%d) - 🏆 <b><font color='#1B5E20'>%s (%d)</font></b> | ⏱ %s (%s)",
                        i + 1, nameJ1, scoreJ1, nombreJ2, scoreJ2, duracion, r.getFechaHora());
            } else {
                htmlText = String.format(Locale.getDefault(),
                        "%d. 🤝 %s (%d) = %s (%d) [%s] | ⏱ %s (%s)",
                        i + 1, nameJ1, scoreJ1, nombreJ2, scoreJ2, getString(R.string.texto_empate), duracion, r.getFechaHora());
            }

            items[i] = android.text.Html.fromHtml(htmlText, android.text.Html.FROM_HTML_MODE_LEGACY);
        }

        String textoBotonOrden = esOrdenAscendente ? getString(R.string.orden_asc_texto) : getString(R.string.orden_desc_texto);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        android.widget.LinearLayout titleLayout = new android.widget.LinearLayout(this);
        titleLayout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        titleLayout.setPadding(60, 50, 60, 40); // Márgenes

        android.widget.TextView tvTitulo = new android.widget.TextView(this);
        tvTitulo.setText(getString(R.string.txtOpcionMejoresResultados) + " - " + titulo + flechaOrden);
        tvTitulo.setTextSize(20);
        tvTitulo.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitulo.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        android.widget.TextView tvCerrar = new android.widget.TextView(this);
        tvCerrar.setText("✕");
        tvCerrar.setTextColor(getColor(android.R.color.black));
        tvCerrar.setTextSize(22);
        tvCerrar.setTypeface(null, android.graphics.Typeface.BOLD);
        tvCerrar.setPadding(30, 0, 10, 0);

        titleLayout.addView(tvTitulo);
        titleLayout.addView(tvCerrar);

        builder.setCustomTitle(titleLayout)
                .setItems(items, null)
                .setPositiveButton(textoBotonOrden, (dialog, which) -> {
                    esOrdenAscendente = !esOrdenAscendente;
                    mostrarMejoresResultadosConFiltro();
                })
                .setNeutralButton(R.string.boton_filtrar, (dialog, which) -> abrirDialogoFiltro())
                .setNegativeButton(R.string.txtOpcionEliminarHistorial, (dialog, which) -> mostrarDialogoBorrarHistorial());

        AlertDialog dialog = builder.create();
        tvCerrar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void abrirDialogoFiltro() {
        CharSequence[] opciones = new CharSequence[]{
                getString(R.string.filtro_semillas_ganador),
                getString(R.string.filtro_partidas_ganadas_jugador)
        };

        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_opciones_filtrado)
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        jugadorFiltro = null;
                        mostrarMejoresResultadosConFiltro();
                    } else if (which == 1) {
                        filtrarPorJugador();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void mostrarDialogoBorrarHistorial() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.txtTituloEliminarHistorial)
                .setMessage(R.string.txtMensajeEliminarHistorial)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    ResultadoDao dao = BantumiDatabase.getInstance(this).resultadoDao();
                    new Thread(() -> {
                        dao.eliminarTodo();
                        runOnUiThread(() -> {
                            com.google.android.material.snackbar.Snackbar.make(
                                    findViewById(android.R.id.content),
                                    getString(R.string.txtHistorialEliminado),
                                    com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                            ).show();
                        });
                    }).start();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void filtrarPorJugador() {
        ResultadoDao dao = BantumiDatabase.getInstance(this).resultadoDao();
        List<String> jugadoresList = dao.obtenerJugadoresDisponibles();
        if (jugadoresList == null) {
            jugadoresList = new java.util.ArrayList<>();
        } else {
            jugadoresList = new java.util.ArrayList<>(jugadoresList);
        }
        String nombreJ2 = getString(R.string.txtPlayer2);
        if (!jugadoresList.contains(nombreJ2)) {
            jugadoresList.add(nombreJ2);
        }

        final List<String> finalJugadoresList = jugadoresList;
        if (finalJugadoresList.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), R.string.txtNoResultados, Snackbar.LENGTH_LONG).show();
            return;
        }

        CharSequence[] items = new CharSequence[finalJugadoresList.size()];
        for (int i = 0; i < finalJugadoresList.size(); i++) {
            items[i] = finalJugadoresList.get(i);
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_selecciona_jugador)
                .setItems(items, (dialog, which) -> {
                    jugadorFiltro = finalJugadoresList.get(which);
                    mostrarMejoresResultadosConFiltro();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    public void guardarPartida() {
        if (juegoBantumi == null) return;

        final EditText input = new EditText(this);
        input.setSingleLine(true);
        String fechaHoraDefault = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(new Date());
        input.setText(getString(R.string.app_name) + " " + fechaHoraDefault);

        new AlertDialog.Builder(this)
                .setTitle(R.string.txtTituloGuardarPartida)
                .setMessage(R.string.txtMensajeNombreGuardar)
                .setView(input)
                .setPositiveButton(R.string.txtBotonGuardar, (dialog, which) -> {
                    String nombrePartida = input.getText().toString().trim();
                    if (nombrePartida.isEmpty()) {
                        nombrePartida = "Partida " + fechaHoraDefault;
                    }

                    long segundos = 0;
                    if (partidaIniciada && cronometro != null) {
                        long tiempoMs = SystemClock.elapsedRealtime() - cronometro.getBase();
                        segundos = Math.max(0, tiempoMs / 1000);
                    }

                    String datosSerializados = juegoBantumi.serializa(segundos);
                    String id = String.valueOf(System.currentTimeMillis());

                    android.content.SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
                    String jsonExistente = preferences.getString("prefKeyListaPartidasGuardadas", "[]");
                    List<PartidaGuardada> lista = PartidaGuardada.jsonToList(jsonExistente);
                    lista.add(0, new PartidaGuardada(id, nombrePartida, fechaHoraDefault, datosSerializados));

                    preferences.edit().putString("prefKeyListaPartidasGuardadas", PartidaGuardada.listToJson(lista)).apply();

                    Snackbar.make(
                            findViewById(android.R.id.content),
                            "Partida \"" + nombrePartida + "\" " + R.string.txtGuardadaCorrectamente,
                            Snackbar.LENGTH_LONG
                    ).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    public void recuperarPartida() {
        android.content.SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        String jsonExistente = preferences.getString("prefKeyListaPartidasGuardadas", "[]");
        List<PartidaGuardada> lista = PartidaGuardada.jsonToList(jsonExistente);

        if (lista.isEmpty()) {
            Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.txtSinPartidaGuardada),
                    Snackbar.LENGTH_LONG
            ).show();
            return;
        }

        CharSequence[] items = new CharSequence[lista.size()];
        String nameJ1 = androidx.preference.PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString("prefKeyNombreJugador", getString(R.string.txtPlayer1));
        String nameJ2 = getString(R.string.txtPlayer2);

        for (int i = 0; i < lista.size(); i++) {
            PartidaGuardada p = lista.get(i);
            String detalles = "";
            try {
                String[] partes = p.getDatosSerializados().split(";");
                if (partes.length >= 4) {
                    String[] semillasArr = partes[2].split(",");
                    int scoreJ1 = Integer.parseInt(semillasArr[6]);
                    int scoreJ2 = Integer.parseInt(semillasArr[13]);
                    long seg = Long.parseLong(partes[3]);
                    String tiempoFormatted = String.format(Locale.getDefault(), "%02d:%02d", seg / 60, seg % 60);
                    detalles = " (⏱ " + tiempoFormatted + " | " + nameJ1 + ": " + scoreJ1 + " - " + nameJ2 + ": " + scoreJ2 + ")";
                }
            } catch (Exception e) {
                detalles = "";
            }
            items[i] = (i + 1) + ". " + p.getNombre() + detalles;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.txtTituloRecuperarPartida)
                .setItems(items, (dialog, which) -> {
                    PartidaGuardada pSeleccionada = lista.get(which);

                    new androidx.lifecycle.ViewModelProvider(this).get(es.upm.miw.bantumi.ui.viewmodel.BantumiViewModel.class)
                            .setIdPartidaRecuperada(pSeleccionada.getId());

                    if (juegoBantumi != null) {
                        long tiempoGuardado = juegoBantumi.deserializa(pSeleccionada.getDatosSerializados());
                        partidaIniciada = true;
                        puntuacionGuardada = false;
                        if (cronometro != null) {
                            cronometro.setBase(SystemClock.elapsedRealtime() - (tiempoGuardado * 1000));
                            cronometro.start();
                        }
                        Snackbar.make(
                                findViewById(android.R.id.content),
                                "Partida \"" + pSeleccionada.getNombre() + "\" recuperada", // Ajustado el R.string para evitar fallos de concatenación
                                Snackbar.LENGTH_LONG
                        ).show();
                        if (juegoBantumi.turnoActual() == es.upm.miw.bantumi.dominio.logica.JuegoBantumi.Turno.turnoJ2) {
                            juegoBantumi.juegaComputador();
                        }
                    }
                })
                .setNeutralButton(R.string.txtBotonEliminar, (dialog, which) -> {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.txtOpcionRecuperar)
                            .setMessage(R.string.txtDialogoEliminarPartidas)
                            .setPositiveButton(android.R.string.ok, (d, w) -> {
                                preferences.edit().remove("prefKeyListaPartidasGuardadas").apply();
                                Snackbar.make(
                                        findViewById(android.R.id.content),
                                        getString(R.string.txtPartidasEliminadas),
                                        Snackbar.LENGTH_LONG
                                ).show();
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Acción que se ejecuta al pulsar sobre cualquier hueco
     *
     * @param v Vista pulsada (hueco)
     */
    public void huecoPulsado(@NonNull View v) {
        if (!partidaIniciada) {
            iniciarCronometro();
        }
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
                procesarFinDeJuego();
        }

        if (juegoBantumi.juegoTerminado()) {
            procesarFinDeJuego();
        }
    }

    /**
     * El juego ha territorizado / terminado. Volver a jugar?
     */
    private void finJuego() {
        long duracionSegundos = 0;
        if (cronometro != null) {
            cronometro.stop();
            long tiempoMs = SystemClock.elapsedRealtime() - cronometro.getBase();
            duracionSegundos = Math.max(0, tiempoMs / 1000);
        }

        if (!puntuacionGuardada) {
            puntuacionGuardada = true;
            String nombreJugador = androidx.preference.PreferenceManager
                    .getDefaultSharedPreferences(this)
                    .getString("prefKeyNombreJugador", getString(R.string.txtPlayer1));
            String fechaHora = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(new Date());
            int semillasJ1 = juegoBantumi.getSemillas(6);
            int semillasJ2 = juegoBantumi.getSemillas(13);

            Resultado resultado = new Resultado(
                    nombreJugador, fechaHora, semillasJ1, semillasJ2, duracionSegundos, numInicialSemillas
            );
            BantumiDatabase.getInstance(this)
                    .resultadoDao()
                    .insertar(resultado);
            Log.i(LOG_TAG, "Puntuación guardada en Room: " + nombreJugador + " (" + duracionSegundos + "s) - J1:" + semillasJ1 + " J2:" + semillasJ2);
        }

        android.content.SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);

        String nombreJugador = preferences.getString("prefKeyNombreJugador", getString(R.string.txtPlayer1));

        String texto = (juegoBantumi.getSemillas(6) > 6 * numInicialSemillas)
                ? getString(R.string.txtGana) + " " + nombreJugador
                : getString(R.string.txtGana) + " " + getString(R.string.txtPlayer2);
        if (juegoBantumi.getSemillas(6) == 6 * numInicialSemillas) {
            texto = "¡¡¡ " + getString(R.string.txtEmpate) + " !!!";
        }

        // terminar
        new FinalAlertDialog(texto).show(getSupportFragmentManager(), "ALERT_DIALOG");
    }

    private void procesarFinDeJuego() {
        bantumiVM = new ViewModelProvider(this).get(BantumiViewModel.class);

        String idRecuperada = bantumiVM.getIdPartidaRecuperada();

        if (idRecuperada != null) {
            android.content.SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
            String jsonPartidas = prefs.getString("prefKeyListaPartidasGuardadas", "");

            List<PartidaGuardada> lista = PartidaGuardada.jsonToList(jsonPartidas);

            for (int i = 0; i < lista.size(); i++) {
                if (lista.get(i).getId().equals(idRecuperada)) {
                    lista.remove(i);
                    break;
                }
            }

            prefs.edit().putString("prefKeyListaPartidasGuardadas", PartidaGuardada.listToJson(lista)).apply();

            bantumiVM.setIdPartidaRecuperada(null);
        }

        finJuego();
    }
}
