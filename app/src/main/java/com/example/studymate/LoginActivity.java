package com.example.studymate;

import android.content.Intent;
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
    SessionManager session;

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

        // Initialize database and session
        dbHelper = new DatabaseHelper(this);
        session = new SessionManager(this);

        // Check if user is already logged in (BUT ONLY if session exists)
        if (session.isLoggedIn()) {
            // User is logged in, go to MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Finish the current activity to prevent going back to login
        }

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

                // Save to SessionManager (always save - remember me handled automatically)
                session.createLoginSession(studentId, userName, userEmail, userYear);

                Toast.makeText(this, "Welcome " + userName + "!", Toast.LENGTH_LONG).show();
                cursor.close();

                // Go to MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            Toast.makeText(this, "Invalid Student ID or Password", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkRememberedUser() {
        // Check if user is already logged in using SessionManager
        if (session.isLoggedIn()) {
            String userName = session.getUserName();

            Toast.makeText(this, "Welcome back " + userName + "!", Toast.LENGTH_SHORT).show();

            // Go directly to MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
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