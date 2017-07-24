package com.example.android.booksinventoryapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.booksinventoryapp.Data.BooksContract.BooksEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Cristi on 7/17/2017.
 */

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_BOOK_LOADER = 0;

    private Uri mCurrentBookUri;
    private Uri imageUri;

    private EditText mAuthorEditText;
    private EditText mTitleEditText;
    private EditText mPublisherText;
    private EditText mYearEditText;
    private EditText mSupplierText;
    private EditText mSupplierEmailText;
    private EditText mPriceText;
    private EditText mQuantityText;
    private TextView mExistQuantityText;
    private TextView mImageUri;
    private ImageView mBookImage;

    private boolean mPetHasChanged = false;
    static final int PICK_IMAGE_REQUEST = 1;
    public static final String LOG_TAG = EditorActivity.class.getSimpleName();

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        mPriceText = (EditText) findViewById(R.id.edit_price);
        mQuantityText = (EditText) findViewById(R.id.adjust_quantity);
        mExistQuantityText = (TextView) findViewById(R.id.existing_quantity);
        mExistQuantityText.setText("0");
        mBookImage = (ImageView) findViewById(R.id.book_image);


        mAuthorEditText.setOnTouchListener(mTouchListener);
        mTitleEditText.setOnTouchListener(mTouchListener);
        mPublisherText.setOnTouchListener(mTouchListener);
        mYearEditText.setOnTouchListener(mTouchListener);
        mSupplierText.setOnTouchListener(mTouchListener);
        mSupplierEmailText.setOnTouchListener(mTouchListener);
        mPriceText.setOnTouchListener(mTouchListener);



        mBookImage.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
});

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                imageUri = resultData.getData();

       //         mImageUri.setText(imageUri.toString());
                mBookImage.setImageBitmap(getBitmapFromUri(imageUri));
            }
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mBookImage.getWidth();
        int targetH = mBookImage.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }
    private void saveBook() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space

        String image = imageUri.toString();
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
        if (TextUtils.isEmpty(authorName) || TextUtils.isEmpty(bookTitle) ||
                TextUtils.isEmpty(bookPriceString)) {
            // Since no fields were modified, we can return early without creating a new pet.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            Toast.makeText(this, getString(R.string.editor_valid_entries),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();

        values.put(BooksEntry.COLUMN_IMAGE, image);
        values.put(BooksEntry.COLUMN_AUTHOR, authorName);
        values.put(BooksEntry.COLUMN_TITLE, bookTitle);
        values.put(BooksEntry.COLUMN_PUBLISHER, bookPublisher);
        values.put(BooksEntry.COLUMN_YEAR, bookYear);
        values.put(BooksEntry.COLUMN_SUPPLIER, supplierName);
        values.put(BooksEntry.COLUMN_SUPPLIER_EMAIL, supplierEmail);



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

        if (mCurrentBookUri == null) {

            Uri newUri = getContentResolver().insert(BooksEntry.CONTENT_URI, values);

            if (newUri == null) {

                Toast.makeText(this, getString(R.string.editor_insert_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
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
                showDeleteConfirmationDialog();
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
                BooksEntry.COLUMN_QUANTITY };


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
            String image = cursor.getString(imageColumnIndex);
            String title = cursor.getString(titleColumnIndex);
            String author = cursor.getString(authorColumnIndex);
            String publisher = cursor.getString(publisherColumnIndex);
            String year = cursor.getString(yearColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String supplierEmail = cursor.getString(supplierEmailColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);


            // Update the views on the screen with the values from the database

      //      mBookImage.setImageBitmap(getBitmapFromUri(imageUri));

      //      if (imageUri!= null) {
        //        mBookImage.setImageBitmap(getBitmapFromUri(imageUri));
       //               }


            Bitmap bitmap = null;
          try {
              imageUri = Uri.parse(image);
              bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

          } catch (IOException e) {
             e.printStackTrace();
              Log.e(LOG_TAG, "No image");
          }
          mBookImage.setImageBitmap(bitmap);

            mTitleEditText.setText(title);
            mAuthorEditText.setText(author);
            mPublisherText.setText(publisher);
            mYearEditText.setText(year);
            mSupplierText.setText(supplier);
            mSupplierEmailText.setText(supplierEmail);
            mPriceText.setText(Double.toString(price));
            mExistQuantityText.setText(Integer.toString(quantity));
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
        mPriceText.setText("");
        mQuantityText.setText("");
        mExistQuantityText.setText("0");

    }
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteBook() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentBookUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }


}
