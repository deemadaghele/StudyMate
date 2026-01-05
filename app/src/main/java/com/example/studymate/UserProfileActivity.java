package com.example.studymate;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class UserProfileActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPhone;
    private Button btnLogout;
    private String studentId;
    private DatabaseHelper dbHelper;
    private SessionManager session;
    private Dialog logoutDialog;
    private MediaPlayer logoutSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        // Initialize database and session
        dbHelper = new DatabaseHelper(this);
        session = new SessionManager(this);

        // Get student ID
        studentId = getIntent().getStringExtra("studentId");
        if (studentId == null) {
            Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnLogout = findViewById(R.id.btnLogout);

        // Initialize sound
        logoutSound = MediaPlayer.create(this, R.raw.logout_sound);

        // Load user data
        loadUserData();

        // Handle back button
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showLogoutDialog();
            }
        });

        // Logout button click
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });
    }

    private void loadUserData() {
        Cursor cursor = dbHelper.getUserInfo(studentId);
        if (cursor != null && cursor.moveToFirst()) {
            etFullName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_NAME)));
            etEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_EMAIL)));
            cursor.close();
        }

        // Disable editing
        etFullName.setEnabled(false);
        etEmail.setEnabled(false);
        etPhone.setEnabled(false);
    }

    private void showLogoutDialog() {
        logoutDialog = new Dialog(this);
        logoutDialog.setContentView(R.layout.logout_dialog);
        logoutDialog.setCancelable(false);

        // Make background transparent
        if (logoutDialog.getWindow() != null) {
            logoutDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Get buttons from dialog
        Button btnCancel = logoutDialog.findViewById(R.id.btnCancel);
        Button btnLogoutDialog = logoutDialog.findViewById(R.id.btnLogout);

        // Cancel button
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutDialog.dismiss();
            }
        });

        // Logout button with sound
        btnLogoutDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Play sound
                if (logoutSound != null) {
                    logoutSound.start();
                }

                // Wait then logout
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        logoutDialog.dismiss();
                        logout();
                    }
                }, 500);
            }
        });

        logoutDialog.show();
    }

    private void logout() {
        // Clear the session BEFORE going to login
        if (session != null) {
            session.logoutUser();  // Clear the session
        }

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (logoutSound != null) {
            logoutSound.release();
            logoutSound = null;
        }

        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}