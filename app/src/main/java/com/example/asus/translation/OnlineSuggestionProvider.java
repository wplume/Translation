package com.example.asus.translation;

import android.content.SearchRecentSuggestionsProvider;
import android.widget.Toast;

public class OnlineSuggestionProvider extends SearchRecentSuggestionsProvider {
    public static final String AUTHORITY = "com.example.asus.translation.OnlineSuggestionProvider";
    public static final int MODE = DATABASE_MODE_QUERIES;

    public OnlineSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
