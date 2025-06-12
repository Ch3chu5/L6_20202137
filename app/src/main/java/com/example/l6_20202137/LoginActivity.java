package com.example.l6_20202137;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar Firebase Auth y Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isValidEmail(s.toString())) {
                    tilEmail.setError("Introduce un correo válido (e.j. usuario@gmail.com)");
                } else {
                    tilEmail.setError(null);
                }
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (!isValidEmail(email)) {
                    tilEmail.setError("Introduce un correo válido (e.j. usuario@gmail.com)");
                    return;
                }

                if (password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Introduce tu contraseña", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Mostrar un mensaje de carga
                Toast.makeText(LoginActivity.this, "Verificando credenciales...", Toast.LENGTH_SHORT).show();

                // Deshabilitar el botón para evitar múltiples intentos
                btnLogin.setEnabled(false);

                // Verificar las credenciales con Firebase Authentication
                signIn(email, password);
            }
        });
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    // Habilitar el botón nuevamente
                    btnLogin.setEnabled(true);

                    if (task.isSuccessful()) {
                        // Login exitoso, verificar el estado en Firestore
                        checkUserSessionStatus(mAuth.getCurrentUser().getUid());
                    } else {
                        // Si falla el login, mostrar un mensaje de error específico
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidUserException e) {
                            Toast.makeText(LoginActivity.this, "El correo no está registrado", Toast.LENGTH_SHORT).show();
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            Toast.makeText(LoginActivity.this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(LoginActivity.this, "Error de autenticación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
    }

    private void checkUserSessionStatus(String uid) {
        db.collection("usuarios").document(uid).get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Verificar si el usuario completó el registro
                        String sesionValue = document.getString("sesion");

                        if ("si".equals(sesionValue)) {
                            // Usuario completamente registrado
                            navigateToPanelPrincipal();
                        } else {
                            // El usuario necesita completar el registro
                            Toast.makeText(LoginActivity.this,
                                "Por favor, completa tu registro primero",
                                Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this,
                            "No se encontró información de usuario",
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this,
                        "Error al verificar el estado del usuario",
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void navigateToPanelPrincipal() {
        Intent intent = new Intent(LoginActivity.this, PanelPrincipalActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }

        String emailPattern = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailPattern);
        boolean basicValidation = pattern.matcher(email).matches();

        if (!basicValidation) return false;

        boolean hasValidDomain = email.endsWith("@gmail.com") ||
                                email.endsWith("@hotmail.com") ||
                                email.endsWith("@yahoo.com") ||
                                email.endsWith("@outlook.com") ||
                                email.endsWith("@icloud.com");

        return hasValidDomain;
    }
}
