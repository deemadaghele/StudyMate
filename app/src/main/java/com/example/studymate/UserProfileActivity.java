package com.example.studymate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class UserProfileActivity extends AppCompatActivity {

    // Views
    ImageView btnBack, btnSettings, ivProfilePicture;
    EditText etFullName, etEmail, etPhone;
    MaterialButton btnLogout;

    // Data
    String studentId, userName, userEmail, userYear;
    DatabaseHelper dbHelper;
    SharedPreferences sharedPreferences;

    // Image picker launcher
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;

    // MediaPlayer for logout sound
    private MediaPlayer logoutSoundPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        // Initialize views
        initializeViews();

        // Initialize database and preferences
        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        // Setup image picker
        setupImagePicker();

        // Get user data
        getUserData();

        // Load user info from database
        loadUserInfo();

        // Load saved profile picture if exists
        loadProfilePicture();

        // Set click listeners
        setClickListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSettings = findViewById(R.id.btnSettings);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);

        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            // Set the image to ImageView
                            ivProfilePicture.setImageURI(selectedImageUri);

                            // Save image URI to SharedPreferences
                            saveProfilePicture(selectedImageUri.toString());

                            Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void getUserData() {
        // Get from intent first
        Intent intent = getIntent();
        studentId = intent.getStringExtra("studentId");
        userName = intent.getStringExtra("userName");

        // If not in intent, get from SharedPreferences
        if (studentId == null || studentId.isEmpty()) {
            studentId = sharedPreferences.getString("studentId", "");
            userName = sharedPreferences.getString("userName", "");
        }

        // If still empty, go back
        if (studentId == null || studentId.isEmpty()) {
            Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadUserInfo() {
        Cursor cursor = dbHelper.getUserInfo(studentId);
        if (cursor != null && cursor.moveToFirst()) {
            // Get data from database
            userName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_NAME));
            userEmail = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_EMAIL));
            userYear = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_YEAR));

            // Set data to fields (disabled - read only)
            etFullName.setText(userName);
            etEmail.setText(userEmail);

            // Fields are disabled by default in XML
            etFullName.setEnabled(false);
            etEmail.setEnabled(false);
            etPhone.setEnabled(false);

            cursor.close();
        }
    }

    private void loadProfilePicture() {
        // Load saved image URI from SharedPreferences
        String savedImageUri = sharedPreferences.getString("profilePicture_" + studentId, "");

        if (!savedImageUri.isEmpty()) {
            try {
                Uri imageUri = Uri.parse(savedImageUri);
                ivProfilePicture.setImageURI(imageUri);
            } catch (Exception e) {
                // If error, keep default image
                e.printStackTrace();
            }
        }
    }

    private void saveProfilePicture(String imageUriString) {
        // Save image URI to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("profilePicture_" + studentId, imageUriString);
        editor.apply();
    }

    private void setClickListeners() {
        // Back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Settings button - just show message
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(UserProfileActivity.this,
                        "Settings feature coming soon!",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Profile picture click - open image picker
        ivProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickerDialog();
            }
        });

        // Logout button
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });
    }

    private void showImagePickerDialog() {
        String[] options = {"Choose from Gallery", "Remove Picture"};

        new AlertDialog.Builder(this)
                .setTitle("Change Profile Picture")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Open gallery
                        openImagePicker();
                    } else {
                        // Remove picture - set to default
                        ivProfilePicture.setImageResource(R.drawable.profile);

                        // Remove from SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove("profilePicture_" + studentId);
                        editor.apply();

                        Toast.makeText(UserProfileActivity.this,
                                "Profile picture removed",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void showLogoutDialog() {
        // Inflate custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.logout_dialog, null);

        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Make dialog background transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Get buttons from custom layout
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnLogoutDialog = dialogView.findViewById(R.id.btnLogout);

        // Cancel button
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Logout button with sound
        btnLogoutDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Play logout sound
                playLogoutSound();

                // Wait a bit then logout
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        logout();
                    }
                }, 500); // 500ms delay
            }
        });

        dialog.show();
    }

    private void playLogoutSound() {
        try {
            // Stop previous sound if playing
            if (logoutSoundPlayer != null) {
                if (logoutSoundPlayer.isPlaying()) {
                    logoutSoundPlayer.stop();
                }
                logoutSoundPlayer.release();
                logoutSoundPlayer = null;
            }

            // Create and play new sound
            logoutSoundPlayer = MediaPlayer.create(this, R.raw.logout_sound);

            if (logoutSoundPlayer != null) {
                logoutSoundPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                        logoutSoundPlayer = null;
                    }
                });

                logoutSoundPlayer.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
            // If sound fails, continue without it
        }
    }

    private void logout() {
        // Clear SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Go to login screen
        Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Release MediaPlayer
        if (logoutSoundPlayer != null) {
            if (logoutSoundPlayer.isPlaying()) {
                logoutSoundPlayer.stop();
            }
            logoutSoundPlayer.release();
            logoutSoundPlayer = null;
        }

        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}