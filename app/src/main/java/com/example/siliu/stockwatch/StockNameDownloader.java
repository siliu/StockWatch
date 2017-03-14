package com.example.siliu.stockwatch;

import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 * Created by siliu on 2/27/17.
 */

public class StockNameDownloader extends AsyncTask<String,Integer,String>{

    private static final String TAG = "StockNameDownloader";
    private MainActivity mainActivity;

    private final String stockNameSearchURL = "http://stocksearchapi.com/api";
    private final String myAPIKey = "b892eec387b974dc4cb73efc8a98073f4b6939a0";
    private String searchText;


    public StockNameDownloader(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
    }

    //No stock name data found alert dialog
    public Dialog noStockNameAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setMessage("Symbol Not Found: " + searchText);
        return builder.create();
    }

    //Multiple stock name list dialog
    public Dialog multipleStockNameListDialog( final String[] stockNameArray){
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle("Make a selection: ");
        builder.setItems(stockNameArray, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which) {
                    String newSymbolString = stockNameArray[which];
                    String newStockSymbol = newSymbolString.split("-")[0];
                    String newCompanyName = newSymbolString.split("-")[1];
                    mainActivity.processNewStock(newStockSymbol,newCompanyName);
                }

            });
        builder.setNegativeButton("NEVERMIND", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                Toast.makeText(mainActivity, "Cancel adding stock.", Toast.LENGTH_SHORT).show();
            }
        });
        return builder.create();
    }

    @Override
    protected void onPostExecute(String s) {
        //Parse JSON
        ArrayList<String> stockNameList = parseJSON(s);

        if(stockNameList == null){
            //Display an alert dialog indicating that no data was found for the supplied symbol
            noStockNameAlertDialog().show();
            return;
        }

        if(stockNameList.size() > 1){
            //Display an alert dialog display the array of strings for the use to choose
            multipleStockNameListDialog(stockNameList.toArray(new String[stockNameList.size()])).show();
        }else{
            //Split one string into two pieces: Stock Symbol and Company Name
            String newSymbolString = stockNameList.get(0);
            String newStockSymbol = newSymbolString.split("-")[0];
            String newCompanyName = newSymbolString.split("-")[1];
            mainActivity.processNewStock(newStockSymbol,newCompanyName);
        }
    }

    @Override
    protected String doInBackground(String... params) {

        //Download stock symbol and company name from website
        searchText = params[0];
        Uri.Builder buildURL = Uri.parse(stockNameSearchURL).buildUpon();
        buildURL.appendQueryParameter("api_key", myAPIKey)
                .appendQueryParameter("search_text",searchText);
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
        }catch (Exception e){

            Log.d(TAG, "doInBackground: " + e);

        }

        return sb.toString();
    }

    private ArrayList<String> parseJSON(String s) {

        ArrayList<String> stockSymbolNameList = new ArrayList<>();
        try {
            JSONArray jObjMain = new JSONArray(s);
            for (int i = 0; i < jObjMain.length(); i++) {
                JSONObject jStock = (JSONObject) jObjMain.get(i);
                String symbol = jStock.getString("company_symbol");
                String companyName = jStock.getString("company_name");
                stockSymbolNameList.add(symbol + " - " + companyName);
            }
            return stockSymbolNameList;
        } catch (Exception e) {
            Log.d(TAG, "parseJSON: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
