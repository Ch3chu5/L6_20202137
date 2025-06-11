package com.example.l6_20202137;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CrearPasswordActivity extends AppCompatActivity {

    private static final String TAG = "CrearPasswordActivity";
    private TextInputEditText etPassword, etConfirmPassword;
    private Button btnCrearPassword;

    // Variables para los datos del usuario recibidos
    private String userId;
    private String userEmail;
    private Map<String, Object> userData;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crear_password);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Obtener datos del intent
        userId = getIntent().getStringExtra("user_id");
        userEmail = getIntent().getStringExtra("user_email");
        userData = new HashMap<>();

        // Recuperar todos los datos de usuario enviados desde RegistroActivity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                if (!"user_id".equals(key) && !"user_email".equals(key)) {
                    Object value = extras.get(key);
                    if (value != null) {
                        userData.put(key, value);
                    }
                }
            }
        }

        if (userId == null || userEmail == null) {
            // Si no hay ID de usuario o correo, regresar a la pantalla principal
            Toast.makeText(this, "Error: No se pudo obtener la información del usuario", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Inicializar vistas
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnCrearPassword = findViewById(R.id.btnCrearPassword);

        // Configurar el botón de creación de contraseña
        btnCrearPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarContraseñas()) {
                    guardarContraseñaUsuario();
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private boolean validarContraseñas() {
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Verificar que las contraseñas no estén vacías
        if (password.isEmpty()) {
            etPassword.setError("La contraseña es obligatoria");
            return false;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Debe confirmar la contraseña");
            return false;
        }

        // Verificar que las contraseñas coincidan
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            return false;
        }

        return true;
    }

    private void guardarContraseñaUsuario() {
        String password = etPassword.getText().toString().trim();

        // Mostrar mensaje de carga
        Toast.makeText(this, "Guardando información...", Toast.LENGTH_SHORT).show();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && userEmail.equals(currentUser.getEmail())) {
            // El usuario ya está autenticado, solo necesitamos vincular la contraseña
            vincularContraseña(currentUser, password);
        } else {
            // Podría ser necesario reautenticar o manejar algún caso especial
            Toast.makeText(this, "Error: Las credenciales de usuario no coinciden", Toast.LENGTH_SHORT).show();
        }
    }

    private void vincularContraseña(FirebaseUser user, String password) {
        // Crear una credencial con el correo y la nueva contraseña
        AuthCredential credential = EmailAuthProvider.getCredential(userEmail, password);

        // Vincular esta credencial a la cuenta del usuario
        user.linkWithCredential(credential)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Usuario vinculado correctamente con correo/contraseña");

                    // Ahora guardamos los datos del usuario en Firestore, pero SIN la contraseña
                    guardarDatosUsuarioEnFirestore();
                } else {
                    Log.e(TAG, "Error al vincular usuario con correo/contraseña", task.getException());
                    Toast.makeText(CrearPasswordActivity.this,
                        "Error al establecer la contraseña: " + task.getException().getMessage(),
                        Toast.LENGTH_LONG).show();
                }
            });
    }

    private void guardarDatosUsuarioEnFirestore() {
        // Agregar el estado de sesión al mapa de datos (la contraseña NO se guarda aquí)
        userData.put("sesion", "si");  // Actualizamos el estado a "si"

        // Preservar el correo si está disponible
        if (userEmail != null && !userEmail.isEmpty()) {
            userData.put("correo", userEmail);
        }

        Log.d(TAG, "Guardando datos para el usuario con ID: " + userId);

        // Guardar datos en Firestore (sin la contraseña)
        db.collection("usuarios").document(userId)
            .set(userData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Datos de usuario guardados correctamente");
                Toast.makeText(CrearPasswordActivity.this, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show();

                // Redirigir al usuario al panel principal
                Intent intent = new Intent(CrearPasswordActivity.this, PanelPrincipalActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al guardar los datos del usuario", e);
                Toast.makeText(CrearPasswordActivity.this, "Error al completar el registro: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            });
    }
}
