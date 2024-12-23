package com.phanta.paicentre.connection.paypal.beans;

import com.google.gson.annotations.SerializedName;

public class CustomAmount {

    @SerializedName("total")
    private String total;

    @SerializedName("currency")
    private String currency;

    // Getters and Setters
    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
