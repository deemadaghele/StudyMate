package com.example.studymate;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import androidx.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    public static final String DATABASE_NAME = "ITStudyMate.db";
    public static final int DATABASE_VERSION = 4;

    // Table Names
    public static final String TABLE_NOTES = "Notes";
    public static final String TABLE_FLASHCARDS = "Flashcards";
    public static final String TABLE_USERS = "Users";

    // Notes Table Columns
    public static final String NOTES_ID = "noteID";
    public static final String NOTES_TITLE = "title";
    public static final String NOTES_COURSE_NAME = "course_name";
    public static final String NOTES_CHAPTER = "chapter";
    public static final String NOTES_PDF_PATH = "pdf_path";
    public static final String NOTES_UPLOADED_BY = "uploaded_by";
    public static final String NOTES_UPLOAD_DATE = "upload_date";
    public static final String NOTES_FILE_SIZE = "file_size";

    // Flashcards Table Columns
    public static final String FLASH_ID = "flashID";
    public static final String FLASH_QUESTION = "question";
    public static final String FLASH_ANSWER = "answer";
    public static final String FLASH_COURSE_NAME = "course_name";
    public static final String FLASH_CHAPTER = "chapter";
    public static final String FLASH_DIFFICULTY = "difficulty";
    public static final String FLASH_CREATED_BY = "created_by";
    public static final String FLASH_CREATION_DATE = "creation_date";

    // Users Table Columns
    public static final String USER_ID = "userID";
    public static final String USER_NAME = "name";
    public static final String USER_EMAIL = "email";
    public static final String USER_STUDENT_ID = "student_id";
    public static final String USER_PASSWORD = "password";
    public static final String USER_YEAR = "year";
    public static final String USER_REGISTRATION_DATE = "registration_date";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users Table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USER_NAME + " TEXT NOT NULL, " +
                USER_EMAIL + " TEXT, " +
                USER_STUDENT_ID + " TEXT UNIQUE NOT NULL, " +
                USER_PASSWORD + " TEXT NOT NULL, " +
                USER_YEAR + " TEXT, " +
                USER_REGISTRATION_DATE + " TEXT);";

        // Create Notes Table
        String createNotesTable = "CREATE TABLE " + TABLE_NOTES + " (" +
                NOTES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NOTES_TITLE + " TEXT NOT NULL, " +
                NOTES_COURSE_NAME + " TEXT NOT NULL, " +
                NOTES_CHAPTER + " TEXT, " +
                NOTES_PDF_PATH + " TEXT NOT NULL, " +
                NOTES_UPLOADED_BY + " TEXT, " +
                NOTES_UPLOAD_DATE + " TEXT, " +
                NOTES_FILE_SIZE + " INTEGER);";

        // Create Flashcards Table
        String createFlashcardsTable = "CREATE TABLE " + TABLE_FLASHCARDS + " (" +
                FLASH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FLASH_QUESTION + " TEXT NOT NULL, " +
                FLASH_ANSWER + " TEXT NOT NULL, " +
                FLASH_COURSE_NAME + " TEXT NOT NULL, " +
                FLASH_CHAPTER + " TEXT, " +
                FLASH_DIFFICULTY + " TEXT, " +
                FLASH_CREATED_BY + " TEXT, " +
                FLASH_CREATION_DATE + " TEXT);";

        db.execSQL(createUsersTable);
        db.execSQL(createNotesTable);
        db.execSQL(createFlashcardsTable);

        // Insert default courses
        insertDefaultCourses(db);
    }

    private void insertDefaultCourses(SQLiteDatabase db) {
        String[] defaultCourses = {
                "ERP",
                "MOBILE",
                "JAVA",
                "C++",
                "TQM",
                "Mobile Programming",
                "Data Structures",
                "Database Systems",
                "Operating Systems",
                "Computer Networks",
                "Software Engineering",
                "Web Development",
                "Algorithms"
        };

        for (String course : defaultCourses) {
            ContentValues cv = new ContentValues();
            cv.put(NOTES_COURSE_NAME, course);
            cv.put(NOTES_TITLE, "Sample");
            cv.put(NOTES_CHAPTER, "0");
            cv.put(NOTES_PDF_PATH, "none");
            cv.put(NOTES_UPLOADED_BY, "system");
            cv.put(NOTES_UPLOAD_DATE, getCurrentDate());
            cv.put(NOTES_FILE_SIZE, 0);
            db.insert(TABLE_NOTES, null, cv);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FLASHCARDS);
        onCreate(db);
    }

    // ============= USER METHODS =============

    public boolean insertUser(String name, String email, String studentId,
                              String password, String year) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(USER_NAME, name);
        cv.put(USER_EMAIL, email);
        cv.put(USER_STUDENT_ID, studentId);
        cv.put(USER_PASSWORD, password);
        cv.put(USER_YEAR, year);
        cv.put(USER_REGISTRATION_DATE, getCurrentDate());

        long result = db.insert(TABLE_USERS, null, cv);
        return result != -1;
    }

    public boolean checkUserLogin(String studentId, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS +
                " WHERE " + USER_STUDENT_ID + " = ? AND " +
                USER_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{studentId, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean checkStudentIdExists(String studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS +
                " WHERE " + USER_STUDENT_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{studentId});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public Cursor getUserInfo(String studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS +
                " WHERE " + USER_STUDENT_ID + " = ?";
        return db.rawQuery(query, new String[]{studentId});
    }

    public boolean updateUser(String studentId, String name, String email, String year) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(USER_NAME, name);
        cv.put(USER_EMAIL, email);
        cv.put(USER_YEAR, year);

        int result = db.update(TABLE_USERS, cv,
                USER_STUDENT_ID + " = ?",
                new String[]{studentId});
        return result > 0;
    }

    public boolean changePassword(String studentId, String oldPassword, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (!checkUserLogin(studentId, oldPassword)) {
            return false;
        }

        ContentValues cv = new ContentValues();
        cv.put(USER_PASSWORD, newPassword);

        int result = db.update(TABLE_USERS, cv,
                USER_STUDENT_ID + " = ?",
                new String[]{studentId});
        return result > 0;
    }

    public boolean deleteUser(String studentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_USERS, USER_STUDENT_ID + " = ?",
                new String[]{studentId});
        return result > 0;
    }

    // ============= NOTES METHODS =============

    public boolean insertNote(String title, String courseName, String chapter,
                              String pdfPath, String uploadedBy, long fileSize) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(NOTES_TITLE, title);
        cv.put(NOTES_COURSE_NAME, courseName);
        cv.put(NOTES_CHAPTER, chapter);
        cv.put(NOTES_PDF_PATH, pdfPath);
        cv.put(NOTES_UPLOADED_BY, uploadedBy);
        cv.put(NOTES_UPLOAD_DATE, getCurrentDate());
        cv.put(NOTES_FILE_SIZE, fileSize);

        long result = db.insert(TABLE_NOTES, null, cv);
        return result != -1;
    }

    public Cursor getAllNotesByCourse(String courseName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NOTES +
                " WHERE " + NOTES_COURSE_NAME + " = ?" +
                " ORDER BY " + NOTES_UPLOAD_DATE + " DESC";
        return db.rawQuery(query, new String[]{courseName});
    }

    public Cursor getNotesByUser(String studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NOTES +
                " WHERE " + NOTES_UPLOADED_BY + " = ?" +
                " ORDER BY " + NOTES_UPLOAD_DATE + " DESC";
        return db.rawQuery(query, new String[]{studentId});
    }

    public Cursor getAllNotes() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NOTES +
                " ORDER BY " + NOTES_UPLOAD_DATE + " DESC";
        return db.rawQuery(query, null);
    }

    public Cursor searchNotes(String searchQuery) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NOTES +
                " WHERE " + NOTES_TITLE + " LIKE ? OR " +
                NOTES_COURSE_NAME + " LIKE ?" +
                " ORDER BY " + NOTES_UPLOAD_DATE + " DESC";
        String searchPattern = "%" + searchQuery + "%";
        return db.rawQuery(query, new String[]{searchPattern, searchPattern});
    }

    public int getUserNotesCount(String studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_NOTES +
                " WHERE " + NOTES_UPLOADED_BY + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{studentId});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public boolean deleteNote(int noteId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_NOTES, NOTES_ID + " = ?",
                new String[]{String.valueOf(noteId)});
        return result > 0;
    }

    // ============= FLASHCARDS METHODS =============

    public boolean insertFlashcard(String question, String answer, String courseName,
                                   String chapter, String difficulty, String createdBy) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(FLASH_QUESTION, question);
        cv.put(FLASH_ANSWER, answer);
        cv.put(FLASH_COURSE_NAME, courseName);
        cv.put(FLASH_CHAPTER, chapter);
        cv.put(FLASH_DIFFICULTY, difficulty);
        cv.put(FLASH_CREATED_BY, createdBy);
        cv.put(FLASH_CREATION_DATE, getCurrentDate());

        long result = db.insert(TABLE_FLASHCARDS, null, cv);
        return result != -1;
    }

    public Cursor getAllFlashcardsByCourse(String courseName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_FLASHCARDS +
                " WHERE " + FLASH_COURSE_NAME + " = ?";
        return db.rawQuery(query, new String[]{courseName});
    }

    public Cursor getFlashcardsByChapter(String courseName, String chapter) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_FLASHCARDS +
                " WHERE " + FLASH_COURSE_NAME + " = ? AND " +
                FLASH_CHAPTER + " = ?";
        return db.rawQuery(query, new String[]{courseName, chapter});
    }

    public Cursor getFlashcardsByUser(String studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_FLASHCARDS +
                " WHERE " + FLASH_CREATED_BY + " = ?" +
                " ORDER BY " + FLASH_CREATION_DATE + " DESC";
        return db.rawQuery(query, new String[]{studentId});
    }

    public int getUserFlashcardsCount(String studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_FLASHCARDS +
                " WHERE " + FLASH_CREATED_BY + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{studentId});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public boolean deleteFlashcard(int flashcardId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_FLASHCARDS, FLASH_ID + " = ?",
                new String[]{String.valueOf(flashcardId)});
        return result > 0;
    }

    // ============= GENERAL METHODS =============

    public Cursor getAllCourses() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT DISTINCT " + NOTES_COURSE_NAME +
                " FROM " + TABLE_NOTES +
                " ORDER BY " + NOTES_COURSE_NAME;
        return db.rawQuery(query, null);
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}