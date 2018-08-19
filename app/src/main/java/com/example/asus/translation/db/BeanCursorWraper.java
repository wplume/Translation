package com.example.asus.translation.db;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.example.asus.translation.bean.OfflineWord;

public class BeanCursorWraper extends CursorWrapper {
    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public BeanCursorWraper(Cursor cursor) {
        super(cursor);
    }

}
