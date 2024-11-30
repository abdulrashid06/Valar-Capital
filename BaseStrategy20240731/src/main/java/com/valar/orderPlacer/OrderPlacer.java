package com.valar.orderPlacer;

import com.valar.accountAttributes.AccountAttributes;
import com.valar.application.ValarTrade;
import com.valar.states.State;
import com.valar.states.State;
import com.valar.entities.TradeEntity;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import static com.valar.application.ValarTrade.quantityFreezeMap;

public abstract class OrderPlacer{
    public int id,sno;
    protected ValarTrade valarTrade;
    protected AccountAttributes accountAttributes;
    protected ExecutorService executorService;
    protected ScheduledExecutorService scheduler;

    public OrderPlacer(AccountAttributes accountAttributes, int sno, int id, ValarTrade valarTrade){
        this.accountAttributes = accountAttributes;
        this.id = id;
        this.sno = sno;
        this.valarTrade = valarTrade;
        this.executorService = valarTrade.executorService;
        this.scheduler = valarTrade.scheduler;
    }

    protected class QuantAttribs{
        int actualQuantity,totalQuantity,times,quantAtLast;
        int splitQuantity;
    }


    public QuantAttribs updateQuantityAttribs(int indexType,boolean isExitDuringShift,State item,boolean isQuantityUpdated){
        int splitsAt = quantityFreezeMap.get(indexType);

        QuantAttribs qa = new QuantAttribs();

        qa.splitQuantity = splitsAt;
        if(isQuantityUpdated) qa.totalQuantity = accountAttributes.getQuantAttrib(sno).getAbsoluteDifference();
        else qa.totalQuantity = accountAttributes.getQuantAttrib(sno).netQuantity;
        qa.actualQuantity = accountAttributes.getQuantAttrib(sno).netQuantity;

        if(qa.splitQuantity!=0){
            qa.times = qa.totalQuantity / qa.splitQuantity;

            if(qa.times!=0) {
                int rem = qa.totalQuantity % qa.splitQuantity;
                if(rem!=0) {
                    qa.quantAtLast = rem;
                    qa.times += 1;
                }
            }else{
                qa.splitQuantity = qa.totalQuantity;
                qa.quantAtLast = qa.totalQuantity;
                qa.times = 1;
            }
        }else{
            qa.totalQuantity = 0;
            qa.actualQuantity = 0;
            qa.times = 0;
        }

        return qa;
    }

    protected int[] getQuantity(int quantity,int splitQuantity){//returns times and quantAtLast
        int loop = quantity / splitQuantity,lastQuant=0;
        int rem = quantity%splitQuantity;
        if(rem>0){
            lastQuant = rem;
            loop++;
        }
        return new int[]{loop,lastQuant};
    }

    protected boolean isOrderPlacingOff(){
        return valarTrade.getBod().isTesting() && valarTrade.getBod().isOrderPlaceOff();
    }

    public static double getRoundValue(double d){
        return ((double)Math.round(d * 20))/20;
    }

    public int getID(){
        return id;
    }

    public abstract void scheduleLimitOrdersSplits(int indexType,String ids,char tradeType, boolean isShifted, TradeEntity te, State item, boolean isEntry, boolean isQuantityUpdated);

    protected abstract void placeMarketOrder(int indexType, String tag, QuantAttribs qa, boolean quantityReloaded, State item, boolean isSell, int quan, int id, boolean isSplit, Map<?,?> ordersMap, boolean isQuantityUpdated, boolean isNormalOrder);
}
