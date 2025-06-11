package com.example.l6_20202137;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.l6_20202137.models.Egreso;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EgresoDialogFragment extends DialogFragment {

    private EditText etTitulo, etMonto, etDescripcion, etFecha;
    private Button btnGuardar, btnCancelar;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;

    private Egreso egresoExistente;
    private boolean esEdicion = false;

    private EgresoFragment.OnEgresoGuardadoListener listener;

    public EgresoDialogFragment() {
        // Constructor vacío requerido
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog_MinWidth);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Obtener ID del usuario actual
        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        }

        // Inicializar formato de fecha
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        calendar = Calendar.getInstance();

        // Verificar si es una edición
        Bundle args = getArguments();
        if (args != null && args.containsKey("egreso")) {
            egresoExistente = (Egreso) args.getSerializable("egreso");
            esEdicion = true;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_egreso, container, false);

        // Inicializar vistas
        etTitulo = view.findViewById(R.id.etTitulo);
        etMonto = view.findViewById(R.id.etMonto);
        etDescripcion = view.findViewById(R.id.etDescripcion);
        etFecha = view.findViewById(R.id.etFecha);
        btnGuardar = view.findViewById(R.id.btnGuardar);
        btnCancelar = view.findViewById(R.id.btnCancelar);

        // Configurar título del diálogo
        getDialog().setTitle(esEdicion ? "Editar Egreso" : "Nuevo Egreso");

        // Configurar selector de fecha
        etFecha.setOnClickListener(v -> mostrarSelectorFecha());

        // Si es edición, cargar datos del egreso existente
        if (esEdicion && egresoExistente != null) {
            cargarDatosEgreso();
        } else {
            // Por defecto mostrar la fecha actual
            etFecha.setText(dateFormat.format(calendar.getTime()));
        }

        // Configurar botones
        btnGuardar.setOnClickListener(v -> guardarEgreso());
        btnCancelar.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    private void mostrarSelectorFecha() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        etFecha.setText(dateFormat.format(calendar.getTime()));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void cargarDatosEgreso() {
        // Cargar datos del egreso en los campos
        etTitulo.setText(egresoExistente.getTitulo());
        etMonto.setText(String.format(Locale.getDefault(), "%.2f", egresoExistente.getMonto()));
        etDescripcion.setText(egresoExistente.getDescripcion());

        // Configurar fecha
        if (egresoExistente.getFecha() != null) {
            calendar.setTime(egresoExistente.getFecha());
            etFecha.setText(dateFormat.format(egresoExistente.getFecha()));
        }

        // Deshabilitar edición del título en modo edición
        etTitulo.setEnabled(false);
        etFecha.setEnabled(false);
    }

    private void guardarEgreso() {
        // Validar campos
        if (!validarCampos()) {
            return;
        }

        String titulo = etTitulo.getText().toString().trim();
        double monto = Double.parseDouble(etMonto.getText().toString().trim());
        String descripcion = etDescripcion.getText().toString().trim();
        Date fecha = calendar.getTime();

        if (esEdicion) {
            // Actualizar egreso existente
            actualizarEgreso(monto, descripcion);
        } else {
            // Crear nuevo egreso
            crearNuevoEgreso(titulo, monto, descripcion, fecha);
        }
    }

    private boolean validarCampos() {
        boolean esValido = true;

        // Validar título
        if (TextUtils.isEmpty(etTitulo.getText())) {
            etTitulo.setError("El título es obligatorio");
            esValido = false;
        }

        // Validar monto
        if (TextUtils.isEmpty(etMonto.getText())) {
            etMonto.setError("El monto es obligatorio");
            esValido = false;
        } else {
            try {
                double monto = Double.parseDouble(etMonto.getText().toString());
                if (monto <= 0) {
                    etMonto.setError("El monto debe ser mayor a cero");
                    esValido = false;
                }
            } catch (NumberFormatException e) {
                etMonto.setError("Ingresa un valor numérico válido");
                esValido = false;
            }
        }

        // Validar fecha
        if (TextUtils.isEmpty(etFecha.getText())) {
            etFecha.setError("La fecha es obligatoria");
            esValido = false;
        }

        return esValido;
    }

    private void crearNuevoEgreso(String titulo, double monto, String descripcion, Date fecha) {
        // Crear objeto egreso
        Egreso nuevoEgreso = new Egreso(titulo, monto, descripcion, fecha);

        // Guardar en Firestore
        db.collection("usuarios").document(userId)
            .collection("egresos")
            .add(nuevoEgreso)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(getContext(), "Egreso guardado exitosamente", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onEgresoGuardado();
                }
                dismiss();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Error al guardar el egreso: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            });
    }

    private void actualizarEgreso(double monto, String descripcion) {
        // Solo actualizar campos permitidos: monto y descripción
        db.collection("usuarios").document(userId)
            .collection("egresos")
            .document(egresoExistente.getId())
            .update(
                "monto", monto,
                "descripcion", descripcion
            )
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Egreso actualizado exitosamente", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onEgresoGuardado();
                }
                dismiss();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Error al actualizar el egreso: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            });
    }

    public void setOnEgresoGuardadoListener(EgresoFragment.OnEgresoGuardadoListener listener) {
        this.listener = listener;
    }
}