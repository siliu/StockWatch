package com.example.siliu.stockwatch;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by siliu on 2/27/17.
 */

public class StockAdapter extends RecyclerView.Adapter<StockViewHolder> {

    private static final String TAG = "StockAdapter";
    private List<Stock> stockList;
    private MainActivity mainActivity;

    public StockAdapter(List<Stock> stockList, MainActivity mainActivity) {
        this.stockList = stockList;
        this.mainActivity = mainActivity;
    }

    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_row, parent, false);

        itemView.setOnClickListener(mainActivity);
        itemView.setOnLongClickListener(mainActivity);

        return new StockViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(StockViewHolder holder, int position) {
        Stock stock = stockList.get(position);
        holder.stockSymbol.setText(stock.getStockSymbol());
        holder.companyName.setText(stock.getCompanyName());
        holder.price.setText(Double.toString(stock.getPrice()));
        holder.changePercentage.setText("(" + Double.toString(stock.getChangePercentage()) + "%)");

        int color;
        char symbol = ' ';
        if (stock.getPriceChange() >= 0) {
            symbol = '\u25B2';
            color = ContextCompat.getColor(this.mainActivity.getApplicationContext(), R.color.green);
        } else {
            symbol = '\u25BC';
            color = ContextCompat.getColor(this.mainActivity.getApplicationContext(), R.color.red);
        }
        holder.priceChange.setText(String.format("%s %.2f", symbol, stock.getPriceChange()));
        holder.stockSymbol.setTextColor(color);
        holder.companyName.setTextColor(color);
        holder.price.setTextColor(color);
        holder.priceChange.setTextColor(color);
        holder.changePercentage.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }
}
