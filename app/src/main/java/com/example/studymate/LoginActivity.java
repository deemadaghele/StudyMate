package com.example.studymate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText studentIdInput, passwordInput;
    Button loginBtn;
    CheckBox rememberMe;
    TextView signUpLink, forgotPassword;
    DatabaseHelper dbHelper;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Initialize views
        studentIdInput = findViewById(R.id.studentIdInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);
        rememberMe = findViewById(R.id.rememberMe);
        signUpLink = findViewById(R.id.signUpLink);
        forgotPassword = findViewById(R.id.forgotPassword);

        // Initialize database
        dbHelper = new DatabaseHelper(this);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        // Check if user is already logged in
        checkRememberedUser();

        // Login button click
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Sign up link click
        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });

        // Forgot password click
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Contact admin to reset password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser() {
        String studentId = studentIdInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validation
        if (studentId.isEmpty()) {
            studentIdInput.setError("Student ID is required");
            studentIdInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        // Check credentials using DatabaseHelper method
        if (dbHelper.checkUserLogin(studentId, password)) {
            // Get user info
            Cursor cursor = dbHelper.getUserInfo(studentId);
            if (cursor.moveToFirst()) {
                String userName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_NAME));
                String userYear = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_YEAR));
                String userEmail = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_EMAIL));

                // Save to SharedPreferences if remember me is checked
                if (rememberMe.isChecked()) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.putString("studentId", studentId);
                    editor.putString("userName", userName);
                    editor.putString("userYear", userYear);
                    editor.putString("userEmail", userEmail);
                    editor.apply();
                }

                Toast.makeText(this, "Welcome " + userName + "!", Toast.LENGTH_LONG).show();
                cursor.close();

                // Go to MainActivity
                // Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                // intent.putExtra("studentId", studentId);
                // intent.putExtra("userName", userName);
                // intent.putExtra("userYear", userYear);
                // startActivity(intent);
                // finish();
            }
        } else {
            Toast.makeText(this, "Invalid Student ID or Password", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkRememberedUser() {
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            String studentId = sharedPreferences.getString("studentId", "");
            String userName = sharedPreferences.getString("userName", "");

            Toast.makeText(this, "Welcome back " + userName + "!", Toast.LENGTH_SHORT).show();

            // Go directly to MainActivity
            // Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            // intent.putExtra("studentId", studentId);
            // intent.putExtra("userName", userName);
            // startActivity(intent);
            // finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}