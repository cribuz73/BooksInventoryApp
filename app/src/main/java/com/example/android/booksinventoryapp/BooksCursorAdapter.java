package com.example.android.booksinventoryapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.booksinventoryapp.Data.BooksContract.BooksEntry;

import java.text.DecimalFormat;

/**
 * Created by Cristi on 7/17/2017.
 */

public class BooksCursorAdapter extends CursorAdapter{

    public BooksCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
    }

    @Override
    public void bindView(final View view, Context context, Cursor cursor) {

        TextView idTextView = (TextView) view.findViewById(R.id.book_id);
        TextView titleTextView = (TextView) view.findViewById(R.id.book_title);
        TextView authorTextView = (TextView) view.findViewById(R.id.book_author);
        TextView quantityTextView = (TextView) view.findViewById(R.id.book_quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.book_price);
        final Button saleButton = (Button)view.findViewById(R.id.saleButton);


        int idColumnIndex = cursor.getColumnIndex(BooksEntry._ID);
        int titleColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_TITLE);
        int authorColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_AUTHOR);
        int quantityColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_PRICE);

       final String bookID = cursor.getString(idColumnIndex);
        String bookTitle = cursor.getString(titleColumnIndex);
        String bookAuthor = cursor.getString(authorColumnIndex);
        final int intBookQuantity = cursor.getInt(quantityColumnIndex);
        double doubleBookPrice = cursor.getDouble(priceColumnIndex);

        String bookQuantity = Integer.toString(intBookQuantity);
        String formattedBookPrice = new DecimalFormat("##,##0.00€").format(doubleBookPrice);

        String formattedBookQuantity = String.format(context.getString(R.string.quantity_adapter)+ " :" + bookQuantity);

            if (TextUtils.isEmpty(bookTitle)) {
            bookTitle = context.getString(R.string.unknown_title);
        }

        if (TextUtils.isEmpty(bookAuthor)) {
            bookAuthor = context.getString(R.string.unknown_author);
        }
        idTextView.setText(bookID);
        titleTextView.setText(bookTitle);
        authorTextView.setText(bookAuthor);
        quantityTextView.setText(formattedBookQuantity);
        priceTextView.setText(formattedBookPrice);

        int itemIdIndex = cursor.getColumnIndex(BooksEntry._ID);
        final int itemId = cursor.getInt(itemIdIndex);


        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Uri currentBookUri = ContentUris.withAppendedId(BooksEntry.CONTENT_URI, itemId);
                int newQuantity = intBookQuantity - 1;
                if (newQuantity < 0) {
                    return;
                }
                ContentValues values = new ContentValues();
                values.put(BooksEntry.COLUMN_QUANTITY, newQuantity);

                ContentResolver resolver = view.getContext().getContentResolver();
                resolver.update(currentBookUri, values, null, null);

            }
        });

    }



}
