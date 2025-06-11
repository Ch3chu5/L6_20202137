package com.example.l6_20202137;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private Button btnIniciarSesion;
    private Button btnRegistroFacebook;
    private Button btnRegistroGmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

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

        // Configurar listeners para los botones de registro
        View.OnClickListener registroListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a la pantalla de registro
                Intent intent = new Intent(MainActivity.this, RegistroActivity.class);
                startActivity(intent);
            }
        };

        btnRegistroFacebook.setOnClickListener(registroListener);
        btnRegistroGmail.setOnClickListener(registroListener);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
