package com.example.studymate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {


    TextView tvUserName, tvWelcomeBack;
    ImageView imgProfilePic, btnNotification;
    CardView cardLibrary, cardFlashcards, cardCreateNote, cardBrowseNotes;

    // User data
    String studentId, userName, userYear;
    SessionManager session;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        // Initialize views
        initializeViews();

        // Initialize database and session
        dbHelper = new DatabaseHelper(this);
        session = new SessionManager(this);

        // Check if user is logged in
        if (!session.isLoggedIn()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }

        // Get user data from session
        getUserData();

        // Set user name
        if (userName != null && !userName.isEmpty()) {
            tvUserName.setText(userName);
        }

        // Set click listeners
        setClickListeners();


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
        // Get data from SessionManager
        studentId = session.getStudentId();
        userName = session.getUserName();
        userYear = session.getUserYear();
    }

    private void setClickListeners() {
        // Profile picture click go to profile
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
                startActivity(intent);
            }
        });

        // Flashcards card click - Navigate to CreateFlashCards
        cardFlashcards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateFlashCards.class);
                startActivity(intent);
            }
        });

        // Create Note card click - Navigate to uploadPdfActivity
        cardCreateNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, uploadPdfActivity.class);
                startActivity(intent);
            }
        });

        // Browse Notes card click - Navigate to BrowseNotesActivity
        cardBrowseNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BrowseNotesActivity.class);
                startActivity(intent);
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