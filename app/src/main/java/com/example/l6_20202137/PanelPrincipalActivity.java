package com.example.l6_20202137;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class PanelPrincipalActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel_principal);

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
                    // Cerrar la sesión y volver a la pantalla principal
                    Intent intent = new Intent(PanelPrincipalActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return true;
                }

                return false;
            }
        });
    }
}
