package com.example.l6_20202137;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegistroActivity extends AppCompatActivity {

    private static final String TAG = "RegistroActivity";
    private TextInputEditText etNombres, etTelefono, etDni, etFechaNacimiento;
    private Button btnRegistrarCuenta;
    private Calendar calendar;

    // Variables para los datos del usuario recibidos de Firebase
    private String userId;
    private String userEmail;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);
        
        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Obtener datos del intent
        userId = getIntent().getStringExtra("user_id");
        userEmail = getIntent().getStringExtra("user_email");

        if (userId == null) {
            // Si no hay ID de usuario, regresar a la pantalla principal
            Toast.makeText(this, "Error: No se pudo obtener la información del usuario", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Inicialización de componentes
        etNombres = findViewById(R.id.etNombres);
        etTelefono = findViewById(R.id.etTelefono);
        etDni = findViewById(R.id.etDni);
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        btnRegistrarCuenta = findViewById(R.id.btnRegistrarCuenta);
        
        calendar = Calendar.getInstance();
        
        // Configurar el campo de fecha para mostrar el DatePickerDialog
        etFechaNacimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
        
        // Configurar el botón de registro
        btnRegistrarCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarCampos()) {
                    // Guardar los datos en Firestore y actualizar el estado de sesión
                    guardarDatosUsuario();
                }
            }
        });
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        
                        // Formatear la fecha y mostrarla en el EditText
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        etFechaNacimiento.setText(dateFormat.format(calendar.getTime()));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    
    private boolean validarCampos() {
        boolean esValido = true;
        
        // Validar que los campos no estén vacíos
        if (etNombres.getText().toString().trim().isEmpty()) {
            etNombres.setError("El nombre es obligatorio");
            esValido = false;
        }
        
        if (etTelefono.getText().toString().trim().isEmpty()) {
            etTelefono.setError("El teléfono es obligatorio");
            esValido = false;
        }
        
        if (etDni.getText().toString().trim().isEmpty()) {
            etDni.setError("El DNI es obligatorio");
            esValido = false;
        } else if (etDni.getText().toString().trim().length() != 8) {
            etDni.setError("El DNI debe tener 8 dígitos");
            esValido = false;
        }
        
        if (etFechaNacimiento.getText().toString().trim().isEmpty()) {
            etFechaNacimiento.setError("La fecha de nacimiento es obligatoria");
            esValido = false;
        }
        
        return esValido;
    }

    private void guardarDatosUsuario() {
        // Obtener los datos ingresados
        String nombres = etNombres.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String dni = etDni.getText().toString().trim();
        String fechaNacimiento = etFechaNacimiento.getText().toString().trim();

        // Crear un mapa con los datos a guardar
        Map<String, Object> userData = new HashMap<>();
        userData.put("nombres", nombres);
        userData.put("telefono", telefono);
        userData.put("dni", dni);
        userData.put("fechaNacimiento", fechaNacimiento);
        userData.put("sesion", "si");  // Actualizar el estado de sesión a "si"

        // Preservar el correo si está disponible
        if (userEmail != null && !userEmail.isEmpty()) {
            userData.put("correo", userEmail);
        }

        // Mostrar un mensaje de carga
        Toast.makeText(this, "Guardando información...", Toast.LENGTH_SHORT).show();

        Log.d(TAG, "Intentando guardar datos para el usuario con ID: " + userId);

        // Usar set() en lugar de update() para crear el documento si no existe
        db.collection("usuarios").document(userId)
            .set(userData)  // Usar set() en lugar de update()
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Datos de usuario guardados correctamente");
                Toast.makeText(RegistroActivity.this, "Registro completado con éxito", Toast.LENGTH_SHORT).show();

                // Redirigir al usuario al panel principal
                Intent intent = new Intent(RegistroActivity.this, PanelPrincipalActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al guardar los datos del usuario", e);
                Toast.makeText(RegistroActivity.this, "Error al completar el registro: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            });
    }
}
