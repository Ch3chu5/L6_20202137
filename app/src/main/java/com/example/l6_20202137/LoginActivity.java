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

                Intent intent = new Intent(LoginActivity.this, PanelPrincipalActivity.class);
                startActivity(intent);
                finish();
            }
        });
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
