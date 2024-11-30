package com.valar.utils;

import java.time.LocalTime;

public class ApiInfo {
    String time,accountName,appOrderId;
    String tag,option,transactionType;
    double ltp,limitPrice;
    int quantity;
    String action="";
    public ApiInfo(String[] accountNameAppOrderId, String[] tagAndOption, String transctionType, double ltp, double limitPrice, int quantity){
        this.time = LocalTime.now().toString();
        this.accountName = accountNameAppOrderId[0];
        this.appOrderId = accountNameAppOrderId[1];
        this.tag = tagAndOption[0];
        this.option = tagAndOption[1];
        this.transactionType = transctionType;
        this.ltp = ltp;
        this.limitPrice = limitPrice;
        this.quantity = quantity;
        this.action = "Order";
    }

    public ApiInfo(String accountName, String appOrderId){
        this.time = LocalTime.now().toString();
        this.accountName = accountName;
        this.appOrderId = appOrderId;
        this.action = "Status";
    }

    public void update(String appOrderId){
        this.time = LocalTime.now().toString();
        this.appOrderId = appOrderId;
    }

    public void updatePrices(double ltp,double limitPrice){
        this.ltp = ltp;
        this.limitPrice = limitPrice;
    }

    @Override
    public String toString() {
        String common = time+","+accountName+","+action+","+appOrderId;
        if(this.action.equalsIgnoreCase("Status"))return common;
        else return common+","+tag+","+option+","+transactionType+","+ltp+","+limitPrice+","+quantity;
    }
}
