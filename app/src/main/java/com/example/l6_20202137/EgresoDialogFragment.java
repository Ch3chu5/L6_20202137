package com.example.l6_20202137;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.l6_20202137.models.Egreso;
import com.example.l6_20202137.models.ServicioAlmacenamiento;
import com.example.l6_20202137.models.EjemploUsoServicio;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EgresoDialogFragment extends DialogFragment {

    private static final int REQUEST_IMAGE_PICK = 1001;

    private EditText etTitulo, etMonto, etDescripcion, etFecha;
    private Button btnGuardar, btnCancelar, btnSeleccionarImagen;
    private ImageView ivComprobantePreview;
    private TextView tvEstadoComprobante;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;

    // Servicios para Supabase
    private ServicioAlmacenamiento servicioAlmacenamiento;
    private EjemploUsoServicio ejemploUsoServicio;

    private Egreso egresoExistente;
    private boolean esEdicion = false;
    private boolean imagenSeleccionada = false;

    private EgresoFragment.OnEgresoGuardadoListener listener;

    public EgresoDialogFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog_MinWidth);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Inicializar servicios de Supabase
        servicioAlmacenamiento = new ServicioAlmacenamiento();
        ejemploUsoServicio = new EjemploUsoServicio();

        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        }

        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        calendar = Calendar.getInstance();

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

        etTitulo = view.findViewById(R.id.etTitulo);
        etMonto = view.findViewById(R.id.etMonto);
        etDescripcion = view.findViewById(R.id.etDescripcion);
        etFecha = view.findViewById(R.id.etFecha);
        btnGuardar = view.findViewById(R.id.btnGuardar);
        btnCancelar = view.findViewById(R.id.btnCancelar);
        btnSeleccionarImagen = view.findViewById(R.id.btnSeleccionarImagen);
        ivComprobantePreview = view.findViewById(R.id.ivComprobantePreview);
        tvEstadoComprobante = view.findViewById(R.id.tvEstadoComprobante);

        getDialog().setTitle(esEdicion ? "Editar Egreso" : "Nuevo Egreso");

        etFecha.setOnClickListener(v -> mostrarSelectorFecha());

        if (esEdicion && egresoExistente != null) {
            cargarDatosEgreso();
        } else {
            etFecha.setText(dateFormat.format(calendar.getTime()));
        }

        btnGuardar.setOnClickListener(v -> guardarEgreso());
        btnCancelar.setOnClickListener(v -> dismiss());
        btnSeleccionarImagen.setOnClickListener(v -> seleccionarImagen());

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
        etTitulo.setText(egresoExistente.getTitulo());
        etMonto.setText(String.format(Locale.getDefault(), "%.2f", egresoExistente.getMonto()));
        etDescripcion.setText(egresoExistente.getDescripcion());

        if (egresoExistente.getFecha() != null) {
            calendar.setTime(egresoExistente.getFecha());
            etFecha.setText(dateFormat.format(egresoExistente.getFecha()));
        }

        etTitulo.setEnabled(false);
        etFecha.setEnabled(false);
    }

    private void guardarEgreso() {
        if (!validarCampos()) {
            return;
        }

        String titulo = etTitulo.getText().toString().trim();
        double monto = Double.parseDouble(etMonto.getText().toString().trim());
        String descripcion = etDescripcion.getText().toString().trim();
        Date fecha = calendar.getTime();

        if (esEdicion) {
            actualizarEgreso(monto, descripcion);
        } else {
            crearNuevoEgreso(titulo, monto, descripcion, fecha);
        }
    }

    private boolean validarCampos() {
        boolean esValido = true;

        if (TextUtils.isEmpty(etTitulo.getText())) {
            etTitulo.setError("El título es obligatorio");
            esValido = false;
        }

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

        if (TextUtils.isEmpty(etFecha.getText())) {
            etFecha.setError("La fecha es obligatoria");
            esValido = false;
        }

        return esValido;
    }

    private void crearNuevoEgreso(String titulo, double monto, String descripcion, Date fecha) {
        Egreso nuevoEgreso = new Egreso(titulo, monto, descripcion, fecha);

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

    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == getActivity().RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);

                    // Comprimir imagen
                    Bitmap bitmapComprimido = comprimirImagen(bitmap);

                    // Mostrar vista previa
                    ivComprobantePreview.setImageBitmap(bitmapComprimido);
                    ivComprobantePreview.setVisibility(View.VISIBLE);
                    imagenSeleccionada = true;

                    // Actualizar estado
                    tvEstadoComprobante.setText("Imagen seleccionada, lista para subir");
                    tvEstadoComprobante.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));

                    // Subir imagen a Supabase inmediatamente
                    subirImagenASupabase(bitmapComprimido);

                } catch (IOException e) {
                    Toast.makeText(getContext(), "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private Bitmap comprimirImagen(Bitmap bitmap) {
        int maxWidth = 1200;
        int maxHeight = 1200;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scale = Math.min((float) maxWidth / width, (float) maxHeight / height);

        if (scale < 1.0f) {
            int newWidth = Math.round(width * scale);
            int newHeight = Math.round(height * scale);
            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        }

        return bitmap;
    }

    private void subirImagenASupabase(Bitmap bitmap) {
        try {
            // Convertir a byte array
            byte[] imageBytes = ejemploUsoServicio.bitmapToByteArray(bitmap, Bitmap.CompressFormat.JPEG, 80);

            // Verificar tamaño
            if (imageBytes.length > 5 * 1024 * 1024) {
                Toast.makeText(getContext(), "La imagen es demasiado grande", Toast.LENGTH_LONG).show();
                return;
            }

            // Generar nombre único para egreso
            String nombreArchivo = ejemploUsoServicio.generarNombreArchivo("egreso", userId, "jpg");

            // Actualizar estado: Conectando
            tvEstadoComprobante.setText("Conectando a Supabase...");
            tvEstadoComprobante.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));

            // Conectar y subir
            servicioAlmacenamiento.conectarServicio()
                .thenAccept(conectado -> {
                    if (conectado) {
                        tvEstadoComprobante.setText("Subiendo imagen...");

                        servicioAlmacenamiento.guardarArchivo(nombreArchivo, imageBytes, "image/jpeg")
                            .thenAccept(urlPublica -> {
                                getActivity().runOnUiThread(() -> {
                                    if (urlPublica != null) {
                                        tvEstadoComprobante.setText("✅ Imagen subida exitosamente");
                                        tvEstadoComprobante.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                                        Toast.makeText(getContext(), "Imagen subida a Supabase: " + nombreArchivo, Toast.LENGTH_SHORT).show();
                                    } else {
                                        tvEstadoComprobante.setText("❌ Error al subir imagen");
                                        tvEstadoComprobante.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                        Toast.makeText(getContext(), "Error al subir imagen a Supabase", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            });
                    } else {
                        getActivity().runOnUiThread(() -> {
                            tvEstadoComprobante.setText("❌ Error de conexión");
                            tvEstadoComprobante.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            Toast.makeText(getContext(), "Error de conexión con Supabase", Toast.LENGTH_SHORT).show();
                        });
                    }
                });

        } catch (Exception e) {
            tvEstadoComprobante.setText("❌ Error al procesar imagen");
            tvEstadoComprobante.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            Toast.makeText(getContext(), "Error al procesar imagen", Toast.LENGTH_SHORT).show();
        }
    }

    public void setOnEgresoGuardadoListener(EgresoFragment.OnEgresoGuardadoListener listener) {
        this.listener = listener;
    }
}