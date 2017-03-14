package com.example.siliu.stockwatch;

import java.text.DecimalFormat;

/**
 * Created by siliu on 2/27/17.
 */

public class Stock {

    private String stockSymbol;
    private String companyName;
    private double price;
    private double priceChange;
    private double changePercentage;


    public Stock(String stockSymbol, String companyName, double price, double priceChange, double changePercentage) {
        this.stockSymbol = stockSymbol;
        this.companyName = companyName;
        this.price = price;
        this.priceChange = priceChange;
        this.changePercentage = changePercentage;
    }


    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPriceChange() {
        return priceChange;
    }

    public void setPriceChange(double priceChange) {
        this.priceChange = priceChange;
    }

    public double getChangePercentage() {
        return changePercentage;
    }

    public void setChangePercentage(double changePercentage) {
        this.changePercentage = changePercentage;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Stock) {
            if (stockSymbol.equals(((Stock) obj).getStockSymbol())) {
                return true;
            }
        }
        return false;
    }
}
