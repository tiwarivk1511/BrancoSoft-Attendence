package com.android.brancoattendence;

public class CheckoutRequest {
    private String checkoutTime;

    public CheckoutRequest(String checkoutTime) {
        this.checkoutTime = checkoutTime;
    }

    public String getCheckoutTime() {
        return checkoutTime;
    }

    public void setCheckoutTime(String checkoutTime) {
        this.checkoutTime = checkoutTime;
    }
}
