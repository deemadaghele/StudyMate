package com.example.studymate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import androidx.cardview.widget.CardView;

import android.widget.TextView;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class uploadPdfActivity extends AppCompatActivity {

    private static final int PICK_PDF_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;

    // UI Components
    private ImageView btnBack, btnHelp, btnRemoveFile;
    private TextInputEditText titleEditText, chapterEditText;
    private Spinner courseSpinner;
    private CardView uploadCard, selectedFileCard;
    private TextView tvSelectedFileName, tvSelectedFileSize;
    private Button uploadButton;

    // Database
    private DatabaseHelper dbHelper;

    // File variables
    private Uri selectedPdfUri;
    private String selectedFileName;
    private long selectedFileSize;

    // User info
    private String currentStudentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_notes);

        // Initialize database
        dbHelper = new DatabaseHelper(this);

        // Get current user from SharedPreferences or Intent
        currentStudentId = getIntent().getStringExtra("studentId");
        if (currentStudentId == null) {
            // Try to get from SharedPreferences
            currentStudentId = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    .getString("studentId", "");
        }

        // Initialize views
        initializeViews();

        // Setup course spinner
        setupCourseSpinner();

        // Setup click listeners
        setupClickListeners();

        // Check permissions
        checkPermissions();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        btnHelp = findViewById(R.id.btnHelp);
        btnRemoveFile = findViewById(R.id.btnRemoveFile);

        titleEditText = findViewById(R.id.titleEditText);
        chapterEditText = findViewById(R.id.chapterEditText);

        courseSpinner = findViewById(R.id.courseSpinner);

        uploadCard = findViewById(R.id.uploadCard);
        selectedFileCard = findViewById(R.id.selectedFileCard);

        tvSelectedFileName = findViewById(R.id.tvSelectedFileName);
        tvSelectedFileSize = findViewById(R.id.tvSelectedFileSize);

        uploadButton = findViewById(R.id.uploadButton);
    }

    private void setupCourseSpinner() {
        List<String> courseList = new ArrayList<>();

        // Get distinct courses from database
        Cursor cursor = dbHelper.getAllCourses();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String courseName = cursor.getString(0);
                if (!courseName.equals("Sample") && !courseList.contains(courseName)) {
                    courseList.add(courseName);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        // If no courses found, add default courses
        if (courseList.isEmpty()) {
            courseList.add("ERP");
            courseList.add("MOBILE");
            courseList.add("JAVA");
            courseList.add("C++");
            courseList.add("TQM");
            courseList.add("Mobile Programming");
            courseList.add("Data Structures");
            courseList.add("Database Systems");
            courseList.add("Operating Systems");
            courseList.add("Computer Networks");
            courseList.add("Software Engineering");
            courseList.add("Web Development");
            courseList.add("Algorithms");
        }

        // Create adapter and set to spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                courseList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpinner.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Help button
        btnHelp.setOnClickListener(v -> {
            Toast.makeText(this,
                    "Fill in all required fields (*) and select a PDF file to upload your notes",
                    Toast.LENGTH_LONG).show();
        });

        // Upload card - open file picker
        uploadCard.setOnClickListener(v -> openFilePicker());

        // Remove file button
        btnRemoveFile.setOnClickListener(v -> removeSelectedFile());

        // Upload button
        uploadButton.setOnClickListener(v -> uploadNote());
    }

    private void checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select PDF File"),
                    PICK_PDF_REQUEST
            );
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                selectedPdfUri = data.getData();
                displaySelectedFile();
            }
        }
    }

    private void displaySelectedFile() {
        if (selectedPdfUri != null) {
            // Get file name and size
            Cursor cursor = getContentResolver().query(
                    selectedPdfUri, null, null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);

                selectedFileName = cursor.getString(nameIndex);
                selectedFileSize = cursor.getLong(sizeIndex);

                cursor.close();

                // Check file size (max 10 MB)
                long maxSize = 10 * 1024 * 1024; // 10 MB in bytes
                if (selectedFileSize > maxSize) {
                    Toast.makeText(this,
                            "File size exceeds 10 MB limit",
                            Toast.LENGTH_SHORT).show();
                    removeSelectedFile();
                    return;
                }

                // Display file info
                tvSelectedFileName.setText(selectedFileName);
                tvSelectedFileSize.setText(formatFileSize(selectedFileSize) + " • Ready");

                // Show selected file card, hide upload card
                uploadCard.setVisibility(View.GONE);
                selectedFileCard.setVisibility(View.VISIBLE);
            }
        }
    }

    private void removeSelectedFile() {
        selectedPdfUri = null;
        selectedFileName = null;
        selectedFileSize = 0;

        // Hide selected file card, show upload card
        selectedFileCard.setVisibility(View.GONE);
        uploadCard.setVisibility(View.VISIBLE);
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }

    private void uploadNote() {
        // Get input values
        String title = titleEditText.getText().toString().trim();
        String chapter = chapterEditText.getText().toString().trim();
        String course = courseSpinner.getSelectedItem().toString();

        // Validate inputs
        if (title.isEmpty()) {
            titleEditText.setError("Title is required");
            titleEditText.requestFocus();
            return;
        }

        if (chapter.isEmpty()) {
            chapterEditText.setError("Chapter number is required");
            chapterEditText.requestFocus();
            return;
        }

        if (selectedPdfUri == null) {
            Toast.makeText(this, "Please select a PDF file", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentStudentId == null || currentStudentId.isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save PDF file to internal storage
        String savedFilePath = savePdfToInternalStorage();

        if (savedFilePath != null) {
            // Insert note to database
            boolean isInserted = dbHelper.insertNote(
                    title,
                    course,
                    chapter,
                    savedFilePath,
                    currentStudentId,
                    selectedFileSize
            );

            if (isInserted) {
                Toast.makeText(this,
                        "Note uploaded successfully!",
                        Toast.LENGTH_SHORT).show();

                // Clear form
                clearForm();

                // Go back or refresh
                finish();
            } else {
                Toast.makeText(this,
                        "Failed to upload note",
                        Toast.LENGTH_SHORT).show();

                // Delete saved file if database insertion failed
                File file = new File(savedFilePath);
                if (file.exists()) {
                    file.delete();
                }
            }
        } else {
            Toast.makeText(this,
                    "Failed to save PDF file",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String savePdfToInternalStorage() {
        try {
            // Create directory for notes if it doesn't exist
            File notesDir = new File(getFilesDir(), "notes");
            if (!notesDir.exists()) {
                notesDir.mkdirs();
            }

            // Create file with unique name
            String fileName = System.currentTimeMillis() + "_" + selectedFileName;
            File destFile = new File(notesDir, fileName);

            // Copy file from URI to internal storage
            InputStream inputStream = getContentResolver().openInputStream(selectedPdfUri);
            FileOutputStream outputStream = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            return destFile.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void clearForm() {
        titleEditText.setText("");
        chapterEditText.setText("");
        courseSpinner.setSelection(0);
        removeSelectedFile();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        "Permission denied. Cannot access files.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}