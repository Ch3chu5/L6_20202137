package com.example.l6_20202137;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar vistas
        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Agregar validación en tiempo real del campo de correo
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No es necesario implementar
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No es necesario implementar
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Validar formato de correo
                if (!isValidEmail(s.toString())) {
                    tilEmail.setError("Introduce un correo válido (e.j. usuario@gmail.com)");
                } else {
                    tilEmail.setError(null);
                }
            }
        });

        // Configurar listener para el botón de inicio de sesión
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                // Validar correo
                if (!isValidEmail(email)) {
                    tilEmail.setError("Introduce un correo válido (e.j. usuario@gmail.com)");
                    return;
                }

                // Validar que se haya introducido una contraseña
                if (password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Introduce tu contraseña", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Navegar a la pantalla principal si pasa las validaciones
                Intent intent = new Intent(LoginActivity.this, PanelPrincipalActivity.class);
                startActivity(intent);
                finish(); // Cerramos esta actividad para que el usuario no pueda volver atrás con el botón de retroceso
            }
        });
    }

    // Método para validar formato de correo electrónico
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }

        // Validar que el correo termine con dominios comunes
        String emailPattern = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailPattern);
        boolean basicValidation = pattern.matcher(email).matches();

        if (!basicValidation) return false;

        // Validar que el dominio sea alguno de los más comunes
        boolean hasValidDomain = email.endsWith("@gmail.com") ||
                                email.endsWith("@hotmail.com") ||
                                email.endsWith("@yahoo.com") ||
                                email.endsWith("@outlook.com") ||
                                email.endsWith("@icloud.com");

        return hasValidDomain;
    }
}
