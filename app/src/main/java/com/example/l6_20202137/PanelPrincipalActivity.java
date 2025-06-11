package com.example.l6_20202137;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class PanelPrincipalActivity extends AppCompatActivity {

    private static final String TAG = "PanelPrincipalActivity";
    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel_principal);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_ingresos) {
                    // Aquí iría el código para mostrar la pantalla de ingresos
                    return true;
                } else if (id == R.id.nav_egresos) {
                    // Aquí iría el código para mostrar la pantalla de egresos
                    return true;
                } else if (id == R.id.nav_resumen) {
                    // Aquí iría el código para mostrar la pantalla de resumen
                    return true;
                } else if (id == R.id.nav_cerrar_sesion) {
                    // Cerrar la sesión y actualizar el estado en Firestore
                    cerrarSesion();
                    return true;
                }

                return false;
            }
        });
    }

    private void cerrarSesion() {
        // Mostrar mensaje de carga
        Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show();

        // Cerrar sesión en Firebase Auth
        mAuth.signOut();

        // Redirigir al usuario a la pantalla principal
        Intent intent = new Intent(PanelPrincipalActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
