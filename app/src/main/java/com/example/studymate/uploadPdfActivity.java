package com.example.studymate;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
public class UploadPdfActivity extends AppCompatActivity {

    private EditText titleEditText;
    private Spinner courseSpinner;
    private Spinner chapterSpinner;
    private Button uploadButton;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_pdf);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Initialize UI components
        titleEditText = findViewById(R.id.titleEditText);
        courseSpinner = findViewById(R.id. courseSpinner);
        chapterSpinner = findViewById(R.id.chapterSpinner);
        uploadButton = findViewById(R.id.uploadButton);

        // Setup spinners (dropdown lists)
        setupCourseSpinner();
        setupChapterSpinner();

        // Upload button click listener
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadNote();
            }
        });
    }

    private void setupCourseSpinner() {
        // Create array of courses
        String[] courses = {
                "Select Course",
                "Mobile Programming",
                "Data Structures",
                "Database Systems",
                "Operating Systems",
                "Computer Networks",
                "Software Engineering"
        };

        // Create adapter and set to spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                courses
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpinner.setAdapter(adapter);
    }

    private void setupChapterSpinner() {
        // Create array of chapters
        String[] chapters = {
                "Select Chapter",
                "Chapter 1",
                "Chapter 2",
                "Chapter 3",
                "Chapter 4",
                "Chapter 5",
                "Chapter 6"
        };

        // Create adapter and set to spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                chapters
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chapterSpinner.setAdapter(adapter);
    }

    private void uploadNote() {
        // Get input values
        String title = titleEditText.getText().toString().trim();
        String course = courseSpinner.getSelectedItem().toString();
        String chapter = chapterSpinner.getSelectedItem().toString();

        // Validation
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (course.equals("Select Course")) {
            Toast.makeText(this, "Please select a course", Toast.LENGTH_SHORT).show();
            return;
        }

        if (chapter.equals("Select Chapter")) {
            Toast.makeText(this, "Please select a chapter", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insert into database
        boolean success = dbHelper.insertNote(
                title,
                course,
                chapter,
                "path/to/pdf",
                "Student Name",
                0
        );

        if (success) {
            Toast.makeText(this, "Note uploaded successfully!", Toast.LENGTH_SHORT).show();
            // Clear inputs
            titleEditText.setText("");
            courseSpinner.setSelection(0);
            chapterSpinner.setSelection(0);
        } else {
            Toast.makeText(this, "Failed to upload", Toast.LENGTH_SHORT).show();
        }
    }
}
