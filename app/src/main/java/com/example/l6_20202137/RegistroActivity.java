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
import java.util.Locale;

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
        
        db = FirebaseFirestore.getInstance();

        userId = getIntent().getStringExtra("user_id");
        userEmail = getIntent().getStringExtra("user_email");

        if (userId == null) {
            Toast.makeText(this, "Error: No se pudo obtener la información del usuario", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        etNombres = findViewById(R.id.etNombres);
        etTelefono = findViewById(R.id.etTelefono);
        etDni = findViewById(R.id.etDni);
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        btnRegistrarCuenta = findViewById(R.id.btnRegistrarCuenta);
        
        calendar = Calendar.getInstance();
        
        etFechaNacimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
        
        btnRegistrarCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarCampos()) {
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
        String nombres = etNombres.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String dni = etDni.getText().toString().trim();
        String fechaNacimiento = etFechaNacimiento.getText().toString().trim();

        Intent intent = new Intent(RegistroActivity.this, CrearPasswordActivity.class);

        intent.putExtra("user_id", userId);
        intent.putExtra("user_email", userEmail);

        intent.putExtra("nombres", nombres);
        intent.putExtra("telefono", telefono);
        intent.putExtra("dni", dni);
        intent.putExtra("fechaNacimiento", fechaNacimiento);

        startActivity(intent);
    }
}

