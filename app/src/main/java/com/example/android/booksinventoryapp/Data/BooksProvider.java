package com.example.android.booksinventoryapp.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.booksinventoryapp.Data.BooksContract.BooksEntry;


/**
 * Created by Cristi on 7/17/2017.
 */

public class BooksProvider extends ContentProvider {

    public static final String LOG_TAG = BooksProvider.class.getSimpleName();

    private static final int BOOKS = 100;

    private static final int BOOK_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(BooksContract.CONTENT_AUTHORITY, BooksContract.PATH_BOOKS, BOOKS);
        sUriMatcher.addURI(BooksContract.CONTENT_AUTHORITY, BooksContract.PATH_BOOKS + "/#", BOOK_ID);
    }

    private BookSQLite mDbHelper;


    @Override
    public boolean onCreate() {
        mDbHelper = new BookSQLite(getContext());
        return true;    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                cursor = database.query(BooksEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case BOOK_ID:
                selection = BooksEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(BooksEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }


        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return BooksEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return BooksEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }
    private Uri insertBook(Uri uri, ContentValues values) {

//        String title = values.getAsString(BooksEntry.COLUMN_TITLE);
//        if (title == null) {
 //           throw new IllegalArgumentException("Book requires a title");
 //       }

 //       String author = values.getAsString(BooksEntry.COLUMN_AUTHOR);
 //       if (author == null) {
 //           throw new IllegalArgumentException("Book requires an author");
 //       }

//        Double price = values.getAsDouble(BooksEntry.COLUMN_PRICE);
 //       if (price <= 0) {
  //          throw new IllegalArgumentException("Insert a valid price");
   //     }

     //   Integer quantity = values.getAsInteger(BooksEntry.COLUMN_QUANTITY);
     //   if (quantity <= 0) {
      //      throw new IllegalArgumentException("Insert a valid quantity");
       // }

       // String supplierEmail = values.getAsString(BooksEntry.COLUMN_SUPPLIER_EMAIL);
       // String supplierEmailValidation = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
       // if (supplierEmail.matches(supplierEmailValidation)) {
       //     Toast.makeText(getContext(), "Valid email address", Toast.LENGTH_SHORT).show();

         //   throw new IllegalArgumentException("Insert a valid email address");
       // }

        // No need to check the breed, any value is valid (including null).

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new pet with the given values
        long id = database.insert(BooksEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,String[] selectionArgs) {
        return 0;
    }
}
