package es.upm.miw.bantumi.ui.fragmentos;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;
import androidx.fragment.app.DialogFragment;

import es.upm.miw.bantumi.R;
import es.upm.miw.bantumi.ui.actividades.MainActivity;

public class RecoverAlertDialog extends DialogFragment {

    private final Runnable onConfirm;

    public RecoverAlertDialog(Runnable onConfirm) {
        this.onConfirm = onConfirm;
    }

    @NonNull
    @Override
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        final MainActivity main = (MainActivity) requireActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(main);
        builder
                .setTitle(R.string.txtOpcionRecuperar)
                .setMessage(R.string.txtDialogoRecuperarPregunta)
                .setPositiveButton(
                        getString(android.R.string.ok),
                        (dialog, which) -> {
                            if (onConfirm != null) {
                                onConfirm.run();
                            }
                        }
                )
                .setNegativeButton(
                        getString(android.R.string.cancel),
                        null
                );

        return builder.create();
    }
}
