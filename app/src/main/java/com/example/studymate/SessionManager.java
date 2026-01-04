package com.example.studymate;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "StudyMateSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_STUDENT_ID = "studentId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_YEAR = "userYear";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Create login session
    public void createLoginSession(String studentId, String name, String email, String year) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_STUDENT_ID, studentId);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_YEAR, year);
        editor.apply();
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Get logged in student ID
    public String getStudentId() {
        return prefs.getString(KEY_STUDENT_ID, null);
    }

    // Get logged in user name
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "Guest");
    }

    // Get logged in user email
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    // Get logged in user year
    public String getUserYear() {
        return prefs.getString(KEY_USER_YEAR, "");
    }

    // Clear session (logout)
    public void logoutUser() {
        editor.clear();
        editor.apply();
    }
}