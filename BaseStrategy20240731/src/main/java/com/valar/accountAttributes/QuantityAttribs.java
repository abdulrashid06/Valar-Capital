package com.valar.accountAttributes;

public class QuantityAttribs {
    public int quantity,netQuantity, prevNetQuantity =-1;
    private int diff;
    private int id;

    public QuantityAttribs(int index){
        this.id = index;
    }

    public synchronized int reloadQuantities(int quantity,int multiplier){
        prevNetQuantity = this.netQuantity;
        this.quantity = quantity;
        this.netQuantity = quantity * multiplier;
        return quantity;
    }

    public boolean checkForChangeInQuantity(){
        if(prevNetQuantity !=-1 && prevNetQuantity != netQuantity)
            return true;
        else if(prevNetQuantity ==-1) prevNetQuantity = netQuantity;
        return false;
    }

    public void setDiffAsQuantity(){
        diff = netQuantity;
        prevNetQuantity = netQuantity;
    }

    public int getUpdatedQuantity(){
        diff = netQuantity - prevNetQuantity;
        prevNetQuantity = netQuantity;
        return diff;
    }

    public int getAbsoluteDifference(){
        return Math.abs(diff);
    }

    public int getDiff(){
        return diff;
    }
}
