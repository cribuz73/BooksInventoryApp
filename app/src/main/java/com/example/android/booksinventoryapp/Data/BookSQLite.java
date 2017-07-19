package com.example.android.booksinventoryapp.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.booksinventoryapp.Data.BooksContract.BooksEntry;

/**
 * Created by Cristi on 7/17/2017.
 */

public class BookSQLite extends SQLiteOpenHelper {

    public static final String LOG_TAG = BookSQLite.class.getSimpleName();

    private static final String DATABASE_NAME = "bookshelf.db";
    private static final int DATABASE_VERSION = 1;

    public BookSQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v(LOG_TAG, "Se initiaza crearea BAZEI DE DATE");

        String SQL_CREATE_BOOKS_TABLE =  "CREATE TABLE " + BooksEntry.TABLE_NAME + " ("
                + BooksEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BooksEntry.COLUMN_IMAGE + " BLOB, "
                + BooksEntry.COLUMN_TITLE + " TEXT NOT NULL, "
                + BooksEntry.COLUMN_AUTHOR + " TEXT NOT NULL, "
                + BooksEntry.COLUMN_PUBLISHER + " TEXT, "
                + BooksEntry.COLUMN_YEAR + " INTEGER, "
                + BooksEntry.COLUMN_PRICE + " REAL NOT NULL DEFAULT 0, "
                + BooksEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + BooksEntry.COLUMN_SUPPLIER + " TEXT, "
                + BooksEntry.COLUMN_SUPPLIER_EMAIL + " TEXT);";

        db.execSQL(SQL_CREATE_BOOKS_TABLE);
        Log.v(LOG_TAG, "S-a creat BAZA DE DATE");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
