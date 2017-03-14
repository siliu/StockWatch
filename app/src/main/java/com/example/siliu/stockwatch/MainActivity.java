package com.example.siliu.stockwatch;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "MainActivity";
    private List<Stock> stockList = new ArrayList<>();  // Main content is here
    private SwipeRefreshLayout swiper; // The SwipeRefreshLayout
    private RecyclerView recyclerView; // Layout's recyclerview
    private StockAdapter stockAdapter; // Data to recyclerview adapter
    private DatabaseHandler databaseHandler;
    private ArrayList<String[]> stockSymbolNameList;
    private boolean onStartup;

    private static final String stockWatchURLRoot = "http://www.marketwatch.com/investing/stock/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onStartup = true;
        swiper = (SwipeRefreshLayout) findViewById(R.id.swiper);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doSwipeRefresh();
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        stockAdapter = new StockAdapter(stockList, this);

        recyclerView.setAdapter(stockAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Check network connection, show alert if no network connection
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            //Load data from database
            databaseHandler = new DatabaseHandler(this);
            databaseHandler.dumpLog();
            stockSymbolNameList = databaseHandler.loadStocks();
            updateData(stockSymbolNameList);
        } else {
            String message = "Stocks Cannot Be Loaded Without A Network Connection.";
            noNetworkConnectionAlertDialog(message).show();
        }

        // Make some data - not always needed - used to fill list
        /*
        for (int i = 0; i < 20; i++) {
            stockList.add(new Stock());
        }
        */
    }

    //On Start, load all data from DB and download the new stock data for each stock
    public void updateData(ArrayList<String[]> stockSymbolNameList) {
        for (int i = 0; i < stockSymbolNameList.size(); i++) {
            String stockSymbol = stockSymbolNameList.get(i)[0];
            String companyName = stockSymbolNameList.get(i)[1];
            new StockDataDownloader(this, onStartup).execute(stockSymbol, companyName);
        }
    }

    private void doSwipeRefresh() {

        onStartup = true;

        //Check network connection, show alert if no network connection
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnectedOrConnecting()) {

            //Re-download stock data for each stock in the current list
            List<Stock> newStockList = new ArrayList<>(stockList);
            stockList.clear();
            for (int i = 0; i < newStockList.size(); i++) {
                String stockSymbol = newStockList.get(i).getStockSymbol();
                String companyName = newStockList.get(i).getCompanyName();
                new StockDataDownloader(this, onStartup).execute(stockSymbol, companyName);
            }

            Toast.makeText(this, "Stocks are up-to-date.", Toast.LENGTH_SHORT).show();
        } else {
            String message = "Stocks Cannot Be Updated Without A Network Connection.";
            noNetworkConnectionAlertDialog(message).show();

        }
        swiper.setRefreshing(false);

    }


    @Override
    public void onClick(View v) {
        int position = recyclerView.getChildLayoutPosition(v);
        Stock stock = stockList.get(position);

        //Open a web browser to the Market Watch site for the selected stock
        String stockWatchURL = stockWatchURLRoot + stock.getStockSymbol();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(stockWatchURL));
        startActivity(intent);
    }

    @Override
    public boolean onLongClick(View v) {

        //Delete stock confirmation dialog
        deleteAlertDialog(v).show();
        return false;
    }

    private Dialog deleteAlertDialog(final View v) {
        int position = recyclerView.getChildLayoutPosition(v);
        final Stock stock = stockList.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setPositiveButton(R.string.stock_delete_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (!stockList.isEmpty()) {
                    //Delete the stock entry from DB first, then from the stock list
                    databaseHandler.deleteStock(stock.getStockSymbol());
                    stockList.remove(stock);
                    stockAdapter.notifyDataSetChanged();
                    Toast.makeText(v.getContext(), "Stock is deleted.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton(R.string.stock_delete_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(v.getContext(), "Delete is canceled.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setIcon(R.drawable.ic_delete);
        builder.setTitle("Delete Stock");
        builder.setMessage("Delete stock: " + stock.getStockSymbol() + "?");

        return builder.create();
    }


    //Process new stock symbol and new company name
    public void processNewStock(String newStockSymbol, String newCompanyName) {

        //Download new stock data in StockDataDownloader
        onStartup = false;
        new StockDataDownloader(this, onStartup).execute(newStockSymbol, newCompanyName);
    }

    //On startup, load all stock symbol and company name from the DB and download data for each stock
    public void loadDBStocks(Stock dbStock) {

        stockList.add(dbStock);

        //Collections.sort(stockList);

        Collections.sort(stockList, new Comparator<Stock>() {
            @Override
            public int compare(Stock s1, Stock s2) {
                return s1.getStockSymbol().compareTo(s2.getStockSymbol());
            }
        });

        stockAdapter.notifyDataSetChanged();
    }

    //Add new stock data
    public void addNewStock(Stock newStock) {
        if (newStock == null) {
            noStockDataAlertDialog().show();
            return;
        }

        if (stockList.contains(newStock)) {
            duplicateStockAlertDialog(newStock).show();
            return;
        }

        //Add new stock to stock list
        stockList.add(newStock);

        //Sort new stock list
        Collections.sort(stockList, new Comparator<Stock>() {
            @Override
            public int compare(Stock s1, Stock s2) {
                return s1.getStockSymbol().compareTo(s2.getStockSymbol());
            }
        });

        //Add new stock to DB: symbol and company name
        databaseHandler.addStock(newStock);

        stockAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.addStock:

                //Check network connection
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                    //Open dialog to input the stock symbol to download the stock symbol and companyname
                    addStockSymbolInputDialog().show();
                } else {
                    String message = "Stock Cannot Be Added Without A Network Connection.";
                    noNetworkConnectionAlertDialog(message).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public Dialog addStockSymbolInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        et.setGravity(Gravity.CENTER_HORIZONTAL);
        et.setFilters(new InputFilter[]{new InputFilter.AllCaps()});


        builder.setTitle("Stock Selection");
        builder.setMessage("Please enter a Stock Symbol:");
        builder.setView(et);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(et.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "Symbol cannot be null.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String searchStockSymbol = et.getText().toString();
                new StockNameDownloader(MainActivity.this).execute(searchStockSymbol);
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                Toast.makeText(MainActivity.this, "Adding stock is cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        return builder.create();
    }

    public Dialog noStockDataAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Symbol Not Found");
        builder.setMessage("No data for this stock symbol.");
        return builder.create();
    }

    public Dialog duplicateStockAlertDialog(Stock stock) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // add dialog icon
        builder.setIcon(R.drawable.ic_warning);
        builder.setTitle("Duplicate Stock");
        builder.setMessage("Stock Symbol " + stock.getStockSymbol() + " is already displayed.");
        return builder.create();
    }

    public Dialog noNetworkConnectionAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Network Connection");
        builder.setMessage(message);
        return builder.create();
    }

}
