package com.example.studymate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {

    // Views
    TextView tvUserName, tvWelcomeBack;
    ImageView imgProfilePic, btnNotification;
    CardView cardLibrary, cardFlashcards, cardCreateNote, cardBrowseNotes;

    // User data
    String studentId, userName, userYear;
    SharedPreferences sharedPreferences;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        // Initialize views
        initializeViews();

        // Initialize database and preferences
        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        // Get user data from intent or SharedPreferences
        getUserData();

        // Set user name in UI
        if (userName != null && !userName.isEmpty()) {
            tvUserName.setText(userName);
        }

        // Set click listeners
        setClickListeners();

        // Setup back press handler
        setupBackPressHandler();
    }

    private void initializeViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvWelcomeBack = findViewById(R.id.tvWelcomeBack);
        imgProfilePic = findViewById(R.id.imgProfilePic);
        btnNotification = findViewById(R.id.btnNotification);

        cardLibrary = findViewById(R.id.cardLibrary);
        cardFlashcards = findViewById(R.id.cardFlashcards);
        cardCreateNote = findViewById(R.id.cardCreateNote);
        cardBrowseNotes = findViewById(R.id.cardBrowseNotes);
    }

    private void getUserData() {
        // Try to get data from intent first
        Intent intent = getIntent();
        studentId = intent.getStringExtra("studentId");
        userName = intent.getStringExtra("userName");
        userYear = intent.getStringExtra("userYear");

        // If not in intent, try SharedPreferences
        if (studentId == null || studentId.isEmpty()) {
            studentId = sharedPreferences.getString("studentId", "");
            userName = sharedPreferences.getString("userName", "");
            userYear = sharedPreferences.getString("userYear", "");
        }

        // If still empty, go back to login
        if (studentId == null || studentId.isEmpty()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
    }

    private void setClickListeners() {
        // Profile picture click - go to profile
        imgProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                intent.putExtra("studentId", studentId);
                intent.putExtra("userName", userName);
                startActivity(intent);
            }
        });

        CardView cardProfilePic = findViewById(R.id.cardProfilePic);
        cardProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                intent.putExtra("studentId", studentId);
                intent.putExtra("userName", userName);
                startActivity(intent);
            }
        });

        // Notifications click
        btnNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "No new notifications", Toast.LENGTH_SHORT).show();
            }
        });

        // Library card click - Navigate to LibraryActivity
        cardLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LibraryActivity.class);
                intent.putExtra("studentId", studentId);
                intent.putExtra("userName", userName);
                startActivity(intent);
            }
        });

        // Flashcards card click - Navigate to CreateFlashcardsActivity
        cardFlashcards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateFlashcardActivity.class);
                intent.putExtra("studentId", studentId);
                intent.putExtra("userName", userName);
                startActivity(intent);
            }
        });

        // Create Note card click - Navigate to CreateNotesActivity
        cardCreateNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateNoteActivity.class);
                intent.putExtra("studentId", studentId);
                intent.putExtra("userName", userName);
                startActivity(intent);
            }
        });

        // Browse Notes card click - Navigate to BrowseNotesActivity
        cardBrowseNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BrowseNotesActivity.class);
                intent.putExtra("studentId", studentId);
                intent.putExtra("userName", userName);
                startActivity(intent);
            }
        });
    }

    // Logout functionality
    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clear SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();

                    Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                    // Go back to login
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    // Handle back press using AndroidX's OnBackPressedDispatcher
    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Show logout dialog when back is pressed
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Exit App")
                        .setMessage("Do you want to logout or just exit?")
                        .setPositiveButton("Logout", (dialog, which) -> logout())
                        .setNegativeButton("Just Exit", (dialog, which) -> {
                            // Disable this callback and trigger back press again
                            setEnabled(false);
                            getOnBackPressedDispatcher().onBackPressed();
                        })
                        .setNeutralButton("Cancel", null)
                        .show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}