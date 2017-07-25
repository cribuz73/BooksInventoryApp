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
import android.support.annotation.RequiresApi;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
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
    private EditText mOrderText;
    private TextView mExistQuantityText;
    private ImageView mBookImage;
    private int quantity;
    private int modifyingQuant;

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

        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();


        if (mCurrentBookUri == null) {
            setTitle(getString(R.string.editor_add_book_title));
            invalidateOptionsMenu();
            quantity = 0;
        } else {
            setTitle(getString(R.string.editor_edit_book_title));
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
        mOrderText = (EditText) findViewById(R.id.new_order);
        mExistQuantityText = (TextView) findViewById(R.id.existing_quantity);
        String qstring = Integer.valueOf(quantity).toString();
        mExistQuantityText.setText(qstring);
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

        final Button orderButton = (Button) findViewById(R.id.order);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String order = mOrderText.getText().toString().trim();
                String email = mSupplierEmailText.getText().toString().trim();
                String author = mAuthorEditText.getText().toString().trim();
                String title = mTitleEditText.getText().toString().trim();

                String[] emails = {email};
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, emails);
                intent.putExtra(Intent.EXTRA_SUBJECT, "New order");
                intent.putExtra(Intent.EXTRA_TEXT, "Please send me a new quantity of " + order + " books from " + title + " by " + author + "\nThank you !");

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        final Button decQuantButton = (Button) findViewById(R.id.decrease_button);
        decQuantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantityString = mExistQuantityText.getText().toString().trim();
                String modifyingQuantityString = mQuantityText.getText().toString().trim();
                if (TextUtils.isEmpty(modifyingQuantityString)) {
                    modifyingQuant = 1;
                } else {
                    modifyingQuant = Integer.parseInt(modifyingQuantityString);
                }
                quantity = Integer.parseInt(quantityString);
                decreaseQuantity();

            }
        });
        final Button incQuantButton = (Button) findViewById(R.id.increase_button);
        incQuantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantityString = mExistQuantityText.getText().toString().trim();
                String modifyingQuantityString = mQuantityText.getText().toString().trim();
                if (TextUtils.isEmpty(modifyingQuantityString)) {
                    modifyingQuant = 1;
                } else {
                    modifyingQuant = Integer.parseInt(modifyingQuantityString);
                }
                quantity = Integer.parseInt(quantityString);
                increaseQuantity();

            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {

            if (resultData != null) {
                imageUri = resultData.getData();
                mBookImage.setImageBitmap(getBitmapFromUri(imageUri));
            }
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        int targetW = mBookImage.getWidth();
        int targetH = mBookImage.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

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

        String authorName = mAuthorEditText.getText().toString().trim();
        String bookTitle = mTitleEditText.getText().toString().trim();
        String bookPublisher = mPublisherText.getText().toString().trim();
        String bookYear = mYearEditText.getText().toString().trim();
        String supplierName = mSupplierText.getText().toString().trim();
        String supplierEmail = mSupplierEmailText.getText().toString().trim();
        String bookPriceString = mPriceText.getText().toString().trim();
        String quantityString = mExistQuantityText.getText().toString().trim();

        quantity = Integer.parseInt(quantityString);

        if (TextUtils.isEmpty(authorName)) {
            Toast.makeText(this, getString(R.string.editor_valid_author),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(bookTitle)) {
            Toast.makeText(this, getString(R.string.editor_valid_title),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(bookPriceString)) {
            Toast.makeText(this, getString(R.string.editor_valid_price),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();

        values.put(BooksEntry.COLUMN_AUTHOR, authorName);
        values.put(BooksEntry.COLUMN_TITLE, bookTitle);
        values.put(BooksEntry.COLUMN_PUBLISHER, bookPublisher);
        values.put(BooksEntry.COLUMN_YEAR, bookYear);
        values.put(BooksEntry.COLUMN_SUPPLIER, supplierName);
        values.put(BooksEntry.COLUMN_SUPPLIER_EMAIL, supplierEmail);

        try {
            String image = imageUri.toString();
            if (image != null) {
                values.put(BooksEntry.COLUMN_IMAGE, image);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.editor_valid_image),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        double price = 0;
        if (!TextUtils.isEmpty(bookPriceString)) {
            price = Double.parseDouble(bookPriceString);
        }
        values.put(BooksEntry.COLUMN_PRICE, price);
        values.put(BooksEntry.COLUMN_QUANTITY, quantity);


        //   if (!TextUtils.isEmpty(bookQuantityString)) {
        //   }
        //  values.put(BooksEntry.COLUMN_QUANTITY, quantity);

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

            int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
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
            case R.id.action_save:
                saveBook();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
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


        return new CursorLoader(this,
                mCurrentBookUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int imageColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_IMAGE);
            int titleColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_TITLE);
            int authorColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_AUTHOR);
            int publisherColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_PUBLISHER);
            int yearColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_YEAR);
            int supplierColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_SUPPLIER);
            int supplierEmailColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_SUPPLIER_EMAIL);
            int priceColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_QUANTITY);


            String image = cursor.getString(imageColumnIndex);
            String title = cursor.getString(titleColumnIndex);
            String author = cursor.getString(authorColumnIndex);
            String publisher = cursor.getString(publisherColumnIndex);
            String year = cursor.getString(yearColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String supplierEmail = cursor.getString(supplierEmailColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);


            try {
                imageUri = Uri.parse(image);

                ViewTreeObserver viewTreeObserver = mBookImage.getViewTreeObserver();

                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    public void onGlobalLayout() {
                        mBookImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mBookImage.setImageBitmap(getBitmapFromUri(imageUri));
                    }
                });
            } catch (NullPointerException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "No image");
            }

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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteBook() {
        if (mCurrentBookUri != null) {

            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    public void increaseQuantity() {
        quantity = quantity + modifyingQuant;

        if (mCurrentBookUri == null) {
            Toast.makeText(this, getString(R.string.editor_valid_book_saved),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        ContentValues values = new ContentValues();
        values.put(BooksEntry.COLUMN_QUANTITY, quantity);

        int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);
        mExistQuantityText.setText(Integer.toString(quantity));

        if (rowsAffected == 0) {
            Toast.makeText(this, getString(R.string.quantity_modify_error),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.quantity_increased),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void decreaseQuantity() {
        quantity = quantity - modifyingQuant;
        if (quantity < 0) {
            Toast.makeText(this, getString(R.string.editor_valid_quantity),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (mCurrentBookUri == null) {
            Toast.makeText(this, getString(R.string.editor_valid_book_saved),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        ContentValues values = new ContentValues();
        values.put(BooksEntry.COLUMN_QUANTITY, quantity);
        int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);
        mExistQuantityText.setText(Integer.toString(quantity));

        if (rowsAffected == 0) {
            Toast.makeText(this, getString(R.string.quantity_modify_error),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.quantity_decreased),
                    Toast.LENGTH_SHORT).show();
        }
    }
}









