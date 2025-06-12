package com.example.l6_20202137;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class PanelPrincipalActivity extends AppCompatActivity {

    private static final String TAG = "PanelPrincipalActivity";
    private BottomNavigationView bottomNavigationView;
    private TextView tvBienvenida;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel_principal);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializar vistas
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        tvBienvenida = findViewById(R.id.tvBienvenida);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Fragment selectedFragment = null;

                if (id == R.id.nav_ingresos) {
                    selectedFragment = new IngresoFragment();
                    actualizarMensajeBienvenida("ingresos");
                    return loadFragment(selectedFragment);
                } else if (id == R.id.nav_egresos) {
                    selectedFragment = new EgresoFragment();
                    actualizarMensajeBienvenida("egresos");
                    return loadFragment(selectedFragment);
                } else if (id == R.id.nav_resumen) {
                    selectedFragment = new ResumenFragment();
                    actualizarMensajeBienvenida("resumen");
                    return loadFragment(selectedFragment);
                } else if (id == R.id.nav_cerrar_sesion) {
                    // Cerrar la sesión
                    cerrarSesion();
                    return true;
                }

                return false;
            }
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_ingresos);
        }
    }

    private void actualizarMensajeBienvenida(String seccion) {
        switch (seccion) {
            case "ingresos":
                tvBienvenida.setText("Registre sus ingresos aquí");
                tvBienvenida.setVisibility(TextView.VISIBLE);
                break;
            case "egresos":
                tvBienvenida.setText("Registre sus egresos aquí");
                tvBienvenida.setVisibility(TextView.VISIBLE);
                break;
            case "resumen":
                tvBienvenida.setVisibility(TextView.GONE);
                break;
            default:
                tvBienvenida.setText("¡Bienvenido al panel de finanzas!");
                tvBienvenida.setVisibility(TextView.VISIBLE);
                break;
        }
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void cerrarSesion() {
        Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show();

        mAuth.signOut();

        Intent intent = new Intent(PanelPrincipalActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

