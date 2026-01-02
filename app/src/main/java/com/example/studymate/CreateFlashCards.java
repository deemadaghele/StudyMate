package com.example.studymate;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

package com.example.studymate;
    public class CreateFlashCards extends AppCompatActivity {

        private EditText questionEditText;
        private EditText answerEditText;
        private Spinner courseSpinner;
        private EditText chapterEditText;

        private Button saveButton;
        private Button addMoreButton;

        private DatabaseHelper dbHelper;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.create_flashcards);

            // Initialize database
            dbHelper = new DatabaseHelper(this);

            // Initialize UI components
            questionEditText = findViewById(R.id.questionEditText);
            answerEditText = findViewById(R.id.answerEditText);
            courseSpinner = findViewById(R.id.courseSpinner);
            chapterEditText = findViewById(R.id.chapterEditText);
            saveButton = findViewById(R.id.saveButton);
            addMoreButton = findViewById(R.id.addMoreButton);

            setupSpinners();

            // Save and close
            saveButton.setOnClickListener(v -> {
                if (saveFlashcard()) {
                    finish();
                }
            });

            // Save and add another
            addMoreButton.setOnClickListener(v -> {
                if (saveFlashcard()) {
                    clearForm();
                }
            });
        }

        private void setupSpinners() {
            // Course Spinner
            String[] courses = {
                    "Select Course",
                    "Mobile Programming",
                    "Data Structures",
                    "Database Systems",
                    "Operating Systems",
                    "Computer Networks",
                    "Software Engineering",
                    "Web Development",
                    "Algorithms"
            };

            ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, courses);
            courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            courseSpinner.setAdapter(courseAdapter);


        }

        private boolean saveFlashcard() {
            String question = questionEditText.getText().toString().trim();
            String answer = answerEditText.getText().toString().trim();
            String course = courseSpinner.getSelectedItem().toString();
            String chapter = chapterEditText.getText().toString().trim();

            // Validation
            if (question.isEmpty()) {
                Toast.makeText(this, "Please enter a question", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (answer.isEmpty()) {
                Toast.makeText(this, "Please enter an answer", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (course.equals("Select Course")) {
                Toast.makeText(this, "Please select a course", Toast.LENGTH_SHORT).show();
                return false;
            }

            // Insert into database
            boolean success = dbHelper.insertFlashcard(
                    question,
                    answer,
                    course,
                    chapter,
                    "Current User" // Replace with actual user name
            );

            if (success) {
                Toast.makeText(this, "Flashcard created successfully!", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                Toast.makeText(this, "Failed to create flashcard", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        private void clearForm() {
            questionEditText.setText("");
            answerEditText.setText("");
            chapterEditText.setText("");
            questionEditText.requestFocus();
        }
    }

