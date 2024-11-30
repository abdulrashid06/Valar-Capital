package com.valar.rms_attribs;


import com.valar.utils.ApiInfo;

import static com.valar.application.ValarTrade.limitRange;

public class RMSAttribs {
    public String orderId,body,limitPriceStr;
    public boolean belongsToLimit;
    private boolean isSellOrder;
    public ApiInfo apiInfo;
    public RMSAttribs(ApiInfo apiInfo,boolean belongsToLimit,boolean isSellOrder, String orderId, String body, String limitPriceStr){
        this.apiInfo = apiInfo;
        this.belongsToLimit = belongsToLimit;
        this.isSellOrder = isSellOrder;
        this.orderId = orderId;
        this.body = body;
        this.limitPriceStr = limitPriceStr;
    }

    public void getUpdatedBody(double ltp){
        double limitPrice = limitRange.getLimitPrice(false,isSellOrder,ltp);
        String updatedLimitPriceStr = "  \"limitPrice\": "+limitPrice+",\n";
        body = body.replace(limitPriceStr,updatedLimitPriceStr);
        limitPriceStr = updatedLimitPriceStr;
        apiInfo.updatePrices(ltp,limitPrice);
    }
}
