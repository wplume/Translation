package com.personal.asus.translation;

import android.content.SearchRecentSuggestionsProvider;

public class OnlineSuggestionProvider extends SearchRecentSuggestionsProvider {
    //修改名字的时候，也要记得修改searchable文件
    public static final String AUTHORITY = OnlineSuggestionProvider.class.getPackage() + "." + OnlineSuggestionProvider.class.getName();
    public static final int MODE = DATABASE_MODE_QUERIES;

    public OnlineSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
