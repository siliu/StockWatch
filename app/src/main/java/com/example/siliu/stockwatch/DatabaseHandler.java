package com.example.siliu.stockwatch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by siliu on 2/27/17.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHandler";

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    // DB Name
    private static final String DATABASE_NAME = "StockAppDB";
    // DB Table Name
    private static final String TABLE_NAME = "StockWatchTable";
    ///DB Columns
    private static final String SYMBOL = "StockSymbol";
    private static final String COMPANY = "CompanyName";

    // DB Table Create Code
    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    SYMBOL + " TEXT not null unique," +
                    COMPANY + " TEXT not null) ";

    private SQLiteDatabase database;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        //TODO: Call getWritableDatabase() in async task ???
        database = getWritableDatabase();

        Log.d(TAG, "DatabaseHandler: Connect to database DONE");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.d(TAG, "onCreate: Creating New DB");
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //No requirement for DB upgrade in this assignment
    }

    public void addStock(Stock stock) {

        Log.d(TAG, "addStock: Adding stock " + stock.getStockSymbol());

        ContentValues values = new ContentValues();
        values.put(SYMBOL, stock.getStockSymbol());
        values.put(COMPANY, stock.getCompanyName());

        database.insert(TABLE_NAME, null, values);

        Log.d(TAG, "addStock: Add Complete");
    }

    public void deleteStock(String symbol) {

        Log.d(TAG, "deleteStock: Deleting stock " + symbol);
        int cnt = database.delete(TABLE_NAME, "StockSymbol = ?", new String[]{symbol});

        Log.d(TAG, "deleteStock: " +cnt);
    }

    public ArrayList<String[]> loadStocks() {

        Log.d(TAG, "loadStocks: Load all symbol-company entries from DB");
        ArrayList<String[]> stocks = new ArrayList<>();

        Cursor cursor = database.query(
                TABLE_NAME,
                new String[]{SYMBOL, COMPANY},
                null,
                null,
                null,
                null,
                null);

        if (cursor != null) {
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                String stockSymbol = cursor.getString(0);
                String companyName = cursor.getString(1);
                stocks.add(new String[]{stockSymbol, companyName});
                cursor.moveToNext();
            }
            cursor.close();
        }

        Log.d(TAG, "loadStocks: Done loading all symbol-company entries from DB");

        return stocks;
    }

    public void dumpLog() {

        Cursor cursor = database.rawQuery("select * from " + TABLE_NAME, null);

        if (cursor != null) {
            cursor.moveToFirst();

            Log.d(TAG, "dumpLog: vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");

            for (int i = 0; i < cursor.getCount(); i++) {
                String stockSymbol = cursor.getString(0);
                String companyName = cursor.getString(1);

                Log.d(TAG, "dumpLog: " +
                        String.format("%s %-18s", SYMBOL + ":", stockSymbol) +
                        String.format("%s %-18s", COMPANY + ":", companyName));
                cursor.moveToNext();
            }
            cursor.close();
        }

        Log.d(TAG, "dumpLog: ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
    }

}
