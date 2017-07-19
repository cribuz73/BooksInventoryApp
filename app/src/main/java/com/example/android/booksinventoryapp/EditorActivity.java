package com.example.android.booksinventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.booksinventoryapp.Data.BooksContract;
import com.example.android.booksinventoryapp.Data.BooksContract.BooksEntry;

/**
 * Created by Cristi on 7/17/2017.
 */

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_BOOK_LOADER = 0;

    private Uri mCurrentBookUri;

    private EditText mAuthorEditText;
    private EditText mTitleEditText;
    private EditText mPublisherText;
    private EditText mYearEditText;
    private EditText mSupplierText;
    private EditText mSupplierEmailText;
    private EditText mPriceText;
    private EditText mQuantityText;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.editor_activity);

// Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new pet or editing an existing one.
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        // If the intent DOES NOT contain a pet content URI, then we know that we are
        // creating a new pet.
        if (mCurrentBookUri == null) {
            // This is a new pet, so change the app bar to say "Add a Pet"
            setTitle(getString(R.string.editor_add_book_title));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing pet, so change app bar to say "Edit Pet"
            setTitle(getString(R.string.editor_edit_book_title));

            // Initialize a loader to read the pet data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }

        mAuthorEditText = (EditText) findViewById(R.id.edit_book_author);
        mTitleEditText = (EditText) findViewById(R.id.edit_book_title);
        mPublisherText = (EditText) findViewById(R.id.edit_book_publisher);
        mYearEditText = (EditText) findViewById(R.id.edit_book_year);
        mSupplierText = (EditText) findViewById(R.id.edit_book_supplier);
        mSupplierEmailText = (EditText) findViewById(R.id.edit_book_supplier_email);
        mPriceText = (EditText) findViewById(R.id.book_price);
        mQuantityText = (EditText) findViewById(R.id.book_quantity);
    }

    private void saveBook() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String authorName = mAuthorEditText.getText().toString().trim();
        String bookTitle = mTitleEditText.getText().toString().trim();
        String bookPublisher = mPublisherText.getText().toString().trim();
        String bookYear = mYearEditText.getText().toString().trim();
        String supplierName = mSupplierText.getText().toString().trim();
        String supplierEmail = mSupplierEmailText.getText().toString().trim();
        String bookPriceString = mPriceText.getText().toString().trim();
        String bookQuantityString = mQuantityText.getText().toString().trim();

        // Check if this is supposed to be a new pet
        // and check if all the fields in the editor are blank
        if (mCurrentBookUri == null &&
                TextUtils.isEmpty(authorName) && TextUtils.isEmpty(bookTitle) &&
                TextUtils.isEmpty(bookPriceString) && TextUtils.isEmpty(bookQuantityString)) {
            // Since no fields were modified, we can return early without creating a new pet.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(BooksContract.BooksEntry.COLUMN_AUTHOR, authorName);
        values.put(BooksContract.BooksEntry.COLUMN_TITLE, bookTitle);
        values.put(BooksContract.BooksEntry.COLUMN_PUBLISHER, bookPublisher);
        values.put(BooksContract.BooksEntry.COLUMN_YEAR, bookYear);
        values.put(BooksEntry.COLUMN_SUPPLIER, supplierName);
        values.put(BooksEntry.COLUMN_SUPPLIER_EMAIL, supplierEmail);
        // If the weight is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        double price = 0;
        if (!TextUtils.isEmpty(bookPriceString)) {
            price = Double.parseDouble(bookPriceString);
        }
        values.put(BooksEntry.COLUMN_PRICE, price);

        int quantity = 0;
        if (!TextUtils.isEmpty(bookQuantityString)) {
            quantity = Integer.parseInt(bookQuantityString);
        }
        values.put(BooksEntry.COLUMN_QUANTITY, quantity);

        // Determine if this is a new or existing pet by checking if mCurrentPetUri is null or not
        if (mCurrentBookUri == null) {
            // This is a NEW pet, so insert a new pet into the provider,
            // returning the content URI for the new pet.
            Uri newUri = getContentResolver().insert(BooksEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING pet, so update the pet with content URI: mCurrentPetUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPetUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                //Save pets to database;
                saveBook();
                // Finish editor activity;
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        String[] projection = {
                BooksEntry._ID,
                BooksEntry.COLUMN_IMAGE,
                BooksEntry.COLUMN_TITLE,
                BooksEntry.COLUMN_AUTHOR,
                BooksEntry.COLUMN_PUBLISHER,
                BooksEntry.COLUMN_YEAR,
                BooksEntry.COLUMN_SUPPLIER,
                BooksEntry.COLUMN_SUPPLIER_EMAIL,
                BooksEntry.COLUMN_PRICE,
                BooksEntry.COLUMN_QUANTITY};


        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentBookUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order    }
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int imageColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_IMAGE);
            int titleColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_TITLE);
            int authorColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_AUTHOR);
            int publisherColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_PUBLISHER);
            int yearColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_YEAR);
            int supplierColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_SUPPLIER);
            int supplierEmailColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_SUPPLIER_EMAIL);
            int priceColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_QUANTITY);

            // Extract out the value from the Cursor for the given column index
            String title = cursor.getString(titleColumnIndex);
            String author = cursor.getString(authorColumnIndex);
            String publisher = cursor.getString(publisherColumnIndex);
            String year = cursor.getString(yearColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String supplierEmail = cursor.getString(supplierEmailColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);

            // Update the views on the screen with the values from the database


            mTitleEditText.setText(title);
            mAuthorEditText.setText(author);
            mPublisherText.setText(publisher);
            mYearEditText.setText(year);
            mSupplierText.setText(supplier);
            mSupplierEmailText.setText(supplierEmail);
            mPriceText.setText(Double.toString(price));
            mQuantityText.setText(Integer.toString(quantity));
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTitleEditText.setText("");
        mAuthorEditText.setText("");
        mPublisherText.setText("");
        mYearEditText.setText("");
        mSupplierText.setText("");
        mSupplierEmailText.setText("");
    }
}