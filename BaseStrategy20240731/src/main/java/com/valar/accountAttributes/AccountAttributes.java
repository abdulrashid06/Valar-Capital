package com.valar.accountAttributes;

import com.valar.orderPlacer.OrderPlacer;
import com.zerodhatech.kiteconnect.KiteConnect;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public abstract class AccountAttributes {
    public String broker;
    public OrderPlacer op;

    public int id;
    public String accountName;

    public String clientID;

    public String REQUEST_TOKEN="8tybIv2a0tOcf3gXtOxww7CFdoCj6Olh";
    public String API_KEY,USER_ID="AB1234",API_SECRET;

    LocalTime lastReloadedTime = null;
    Map<Integer,QuantityAttribs> quantityAttribsMap = new HashMap<>();

    public KiteConnect kiteConnect;
    protected String lotValFile;

    public AccountAttributes(String lotValFile,String broker,String accountName,int index){
        this.lotValFile = lotValFile;
        this.broker = broker;
        this.accountName = accountName;
        this.id = index;
    }

    private int getMultiplier(String lotsMultiplier){
        if(lotsMultiplier==null || lotsMultiplier.isEmpty())return 1;
        else try {return Integer.parseInt(lotsMultiplier.split(",")[id+1]);}catch (Exception e){e.printStackTrace();}
        return 1;
    }

    public synchronized void setQuantityAttribsMap(boolean isLotsMultiplierEmpty,Map<Integer,Integer> lotsMap,String lotsMultiplier){
        for(int k:lotsMap.keySet()){
            QuantityAttribs qa = new QuantityAttribs(id);
            quantityAttribsMap.put(k,qa);
            int quantityAndLot = isLotsMultiplierEmpty?0:lotsMap.get(k);
            qa.reloadQuantities(quantityAndLot,getMultiplier(lotsMultiplier));
        }
    }

    public synchronized void reloadQuantities(Map<Integer,Integer> lotsMap,int sno,String lotsMultiplier){
        QuantityAttribs qa = quantityAttribsMap.get(sno);;
        int quantityAndLot = lotsMap.get(sno);
        qa.reloadQuantities(quantityAndLot,getMultiplier(lotsMultiplier));
    }

    public QuantityAttribs getQuantAttrib(int sno){
        return quantityAttribsMap.get(sno);
    }

    @Override
    public String toString() {
        return "AccountAttributes{" +
                ", accountName='" + accountName + '\'' +
                ", broker=" + broker +
                ", id=" + id +
                ", clientID='" + clientID + '\'' +
                ", REQUEST_TOKEN='" + REQUEST_TOKEN + '\'' +
                ", API_KEY='" + API_KEY + '\'' +
                ", USER_ID='" + USER_ID + '\'' +
                ", API_SECRET='" + API_SECRET + '\'' +
                '}';
    }
}
