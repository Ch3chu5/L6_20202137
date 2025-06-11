package com.example.l6_20202137;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 9001;

    private Button btnIniciarSesion;
    private Button btnRegistroFacebook;
    private Button btnRegistroGmail;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Inicializar Firebase Auth y Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Verificar si hay un usuario ya autenticado
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Ya hay un usuario autenticado, verificar su estado de sesión
            checkUserSessionStatus(currentUser);
            return; // Evitar continuar con la inicialización de la pantalla principal
        }

        // Configurar Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Inicializar los botones
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        btnRegistroFacebook = findViewById(R.id.btnRegistroFacebook);
        btnRegistroGmail = findViewById(R.id.btnRegistroGmail);

        // Configurar listener para el botón de inicio de sesión
        btnIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a la pantalla de login
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // Configurar listener para Facebook
        btnRegistroFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a la pantalla de registro
                Intent intent = new Intent(MainActivity.this, RegistroActivity.class);
                startActivity(intent);
            }
        });

        // Configurar listener para el botón de Gmail
        btnRegistroGmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Resultado devuelto al iniciar el Intent desde GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // La autenticación con Google fue exitosa, ahora autenticamos con Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // La autenticación con Google falló
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(MainActivity.this, "Error en la autenticación con Google",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Inicio de sesión exitoso
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null) {
                                saveUserToFirestore(user);
                            }
                        } else {
                            // Error en el inicio de sesión
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Error en la autenticación con Firebase",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser user) {
        try {
            // Verificar si el usuario tiene datos válidos
            String uid = user.getUid();
            String correo = user.getEmail();

            if (uid == null || correo == null) {
                Log.e(TAG, "UID o correo nulos");
                updateUI(user);
                return;
            }

            Log.d(TAG, "Verificando si el usuario ya existe en Firestore: " + uid);

            // Usar runOnUiThread para asegurar que la operación se realiza en el hilo principal
            runOnUiThread(() -> {
                // Primero verificamos si el documento ya existe
                db.collection("usuarios")
                    .document(uid)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // El usuario ya existe, no modificamos sus datos
                                Log.d(TAG, "Usuario ya existe en Firestore, manteniendo datos existentes");
                                // Solo actualizamos el correo si ha cambiado
                                if (!correo.equals(document.getString("correo"))) {
                                    db.collection("usuarios")
                                        .document(uid)
                                        .update("correo", correo)
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Correo actualizado correctamente"));
                                }
                                // Verificamos el estado de sesión directamente
                                checkUserSessionStatus(user);
                            } else {
                                // El usuario no existe, lo creamos con "sesion" en "no"
                                Log.d(TAG, "Usuario nuevo, creando documento en Firestore");
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("correo", correo);
                                userData.put("sesion", "no");

                                db.collection("usuarios")
                                    .document(uid)
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Usuario nuevo guardado correctamente en Firestore");
                                        checkUserSessionStatus(user);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error al guardar el usuario en Firestore", e);
                                        Toast.makeText(MainActivity.this,
                                                "Error al guardar datos: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                        updateUI(user);
                                    });
                            }
                        } else {
                            Log.e(TAG, "Error al verificar si el usuario existe", task.getException());
                            Toast.makeText(MainActivity.this,
                                    "Error al verificar usuario: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                            updateUI(user);
                        }
                    });
            });
        } catch (Exception e) {
            Log.e(TAG, "Error general al guardar usuario", e);
            Toast.makeText(MainActivity.this,
                    "Error inesperado: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            updateUI(user);
        }
    }

    private void checkUserSessionStatus(FirebaseUser user) {
        String uid = user.getUid();

        db.collection("usuarios").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Verificar el valor del campo "sesion"
                            String sesionValue = document.getString("sesion");
                            Log.d(TAG, "Valor de sesión: " + sesionValue);

                            if ("si".equals(sesionValue)) {
                                // Si la sesión es "si", navegar al panel principal
                                navigateToPanelPrincipal();
                            } else {
                                // Si la sesión es "no", navegar a la pantalla de registro
                                navigateToRegistro(user);
                            }
                        } else {
                            Log.d(TAG, "No se encontró el documento del usuario");
                            navigateToRegistro(user);
                        }
                    } else {
                        Log.e(TAG, "Error al obtener documento", task.getException());
                        updateUI(user);
                    }
                });
    }

    private void navigateToPanelPrincipal() {
        Intent intent = new Intent(MainActivity.this, PanelPrincipalActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Finalizar MainActivity para que no se pueda volver atrás
    }

    private void navigateToRegistro(FirebaseUser user) {
        Intent intent = new Intent(MainActivity.this, RegistroActivity.class);
        // Pasar información del usuario a la actividad de registro si es necesario
        intent.putExtra("user_id", user.getUid());
        intent.putExtra("user_email", user.getEmail());
        startActivity(intent);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Usuario autenticado con éxito
            String mensaje = "Bienvenido: " + user.getDisplayName() + "\nCorreo: " + user.getEmail();
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        }
    }
}
