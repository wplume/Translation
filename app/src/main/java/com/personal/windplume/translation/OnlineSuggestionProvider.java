package com.personal.windplume.translation;

import android.content.SearchRecentSuggestionsProvider;
import android.util.Log;

public class OnlineSuggestionProvider extends SearchRecentSuggestionsProvider {
    //修改名字的时候，也要记得修改searchable文件
    public static final String AUTHORITY = OnlineSuggestionProvider.class.getName();
    public static final int MODE = DATABASE_MODE_QUERIES;
    private static final String TAG = OnlineSuggestionProvider.class.getName();

    public OnlineSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
