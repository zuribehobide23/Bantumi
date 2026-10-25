package es.upm.miw.bantumi.ui.actividades;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import es.upm.miw.bantumi.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        android.content.SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        String temaActual = preferences.getString("prefKeyColorTema", "azul");
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
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();

        // Establece las inserciones de recortes de pantalla
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private android.content.SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            EditTextPreference prefNombre = findPreference("prefKeyNombreJugador");
            if (prefNombre != null) {
                prefNombre.setOnBindEditTextListener(editText -> {
                    editText.setSingleLine(true);
                    editText.setFilters(new android.text.InputFilter[]{
                            new android.text.InputFilter.LengthFilter(15)
                    });
                });
            }

            EditTextPreference prefSemillas = findPreference("prefKeyNumInicialSemillas");
            if (prefSemillas != null) {
                prefSemillas.setDialogMessage(R.string.prefMensajeNumSemillas);
                prefSemillas.setOnBindEditTextListener(editText -> {
                    editText.setSingleLine(true);
                    editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                    editText.setHint("Ej. 4");
                    editText.setFilters(new android.text.InputFilter[]{
                            new android.text.InputFilter.LengthFilter(2)
                    });
                });
            }

            preferenceChangeListener = (sharedPreferences, key) -> {
                if ("prefKeyColorTema".equals(key)) {
                    if (getActivity() != null) {
                        getActivity().recreate();
                    }
                }
            };
        }

        @Override
        public void onResume() {
            super.onResume();
            if (getPreferenceManager().getSharedPreferences() != null) {
                getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(preferenceChangeListener);
            }
        }

        @Override
        public void onPause() {
            super.onPause();
            if (getPreferenceManager().getSharedPreferences() != null) {
                getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
            }
        }
    }
}
