package com.example.siliu.stockwatch;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by siliu on 2/27/17.
 */

public class StockDataDownloader extends AsyncTask<String, Integer, String> {

    private static final String TAG = "StockDataDownloader";
    private MainActivity mainActivity;

    private final String stockSearchURL = "http://finance.google.com/finance/info";
    private final String client = "ig";
    private String searchStockSymbol;
    private String searchCompanyName;
    private boolean onStartup;

    public StockDataDownloader(MainActivity mainActivity, boolean onStartup) {
        this.mainActivity = mainActivity;
        this.onStartup = onStartup;
    }

    @Override
    protected void onPostExecute(String s) {
        //Sent the parsed stock object to the main activity to add
        if (onStartup) {
            // Load all stocks in the DB
            mainActivity.loadDBStocks(parseJSON(s));
        } else {
            // Add new stock in the main activity
            mainActivity.addNewStock(parseJSON(s));
        }
    }

    @Override
    protected String doInBackground(String... params) {

        //Download stock finance data from website
        searchStockSymbol = params[0];
        searchCompanyName = params[1];
        Uri.Builder buildURL = Uri.parse(stockSearchURL).buildUpon();
        buildURL.appendQueryParameter("client", client)
                .appendQueryParameter("q", searchStockSymbol);
        String urlToUse = buildURL.toString();

        Log.d(TAG, "doInBackground: " + urlToUse);

        StringBuilder sb = new StringBuilder();

        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            Log.d(TAG, "doInBackground: " + sb.toString());
        } catch (Exception e) {

            Log.d(TAG, "doInBackground: " + e);

        }

        return sb.toString().substring(3);
    }

    private Stock parseJSON(String s) {

        try {
            JSONArray jObjMain = new JSONArray(s);
            JSONObject jStock = (JSONObject) jObjMain.get(0);
            double price = jStock.getDouble("l");
            double priceChange = jStock.getDouble("c");
            double changePercentage = jStock.getDouble("cp");
            return new Stock(searchStockSymbol, searchCompanyName, price, priceChange, changePercentage);

        } catch (Exception e) {
            Log.d(TAG, "parseJSON: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
