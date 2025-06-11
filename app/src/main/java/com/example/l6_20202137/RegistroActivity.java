package com.example.l6_20202137;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RegistroActivity extends AppCompatActivity {

    private TextInputEditText etNombres, etTelefono, etDni, etFechaNacimiento;
    private Button btnRegistrarCuenta;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);
        
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
                    // Navegar al panel principal
                    Intent intent = new Intent(RegistroActivity.this, PanelPrincipalActivity.class);
                    // Eliminar las actividades anteriores de la pila para evitar que el usuario regrese
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
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
}