package com.example.siliu.stockwatch;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by siliu on 2/27/17.
 */

public class StockViewHolder extends RecyclerView.ViewHolder {
    public TextView stockSymbol;
    public TextView companyName;
    public TextView price;
    public TextView priceChange;
    public TextView changePercentage;

    public StockViewHolder(View itemView) {
        super(itemView);
        this.stockSymbol = (TextView) itemView.findViewById(R.id.stockSymbol);
        this.companyName = (TextView) itemView.findViewById(R.id.companyName);
        this.price = (TextView) itemView.findViewById(R.id.price);
        this.priceChange = (TextView) itemView.findViewById(R.id.priceChange);
        this.changePercentage = (TextView) itemView.findViewById(R.id.changePercentage);
    }
}
