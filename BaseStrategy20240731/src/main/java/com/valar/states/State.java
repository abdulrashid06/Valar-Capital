package com.valar.states;

import com.valar.accountAttributes.AccountAttributes;
import com.valar.application.ValarTrade;
import com.valar.entities.Ohlc;
import com.valar.utils.PrintAttribs;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.valar.application.ValarTrade.allAccountAttributes;
import static com.valar.application.ValarTrade.printInfo;

public abstract class State implements Cloneable{

    protected long token,symphonyToken;
    protected String symbol,peOrCe;
    protected Ohlc ohlc = new Ohlc();

    protected SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    protected double ltp;
    protected boolean traded;

    protected Map<String,String> tagsMap = new ConcurrentHashMap<>();
    protected int tradeID;
    protected double premium;
    protected int strike;

    public State(boolean isIndex,long token, long symphonyToken, String peOrCe, String symbol){
        this.token = token;
        this.symphonyToken = symphonyToken;
        this.peOrCe = peOrCe;
        this.symbol = symbol;
        if(!isIndex)
            try{this.strike = Integer.parseInt(symbol.substring(symbol.length()-7,symbol.length()-2));}catch (Exception e){e.printStackTrace();}
    }

    public void setStrike(int strike){
        this.strike = strike;
    }

    public synchronized void addTag(String tagKey,String value){
        tagsMap.put(tagKey,value);
    }

    public void setPremium(double premium){
        this.premium = premium;
    }

    public double getPremium(){
        return premium;
    }

    public int getTradeID(){
        return tradeID;
    }

    public void updateTradeID(){
        tradeID++;
    }

    public int getStrike(){
        return strike;
    }

    public void printOrderInfo(String tagKey, PrintAttribs printAttribs){

        List<AccountAttributes> accountAttributes = allAccountAttributes;
        String quantities="";
        for(AccountAttributes aa : accountAttributes)
            quantities += aa.getQuantAttrib(printAttribs.ksId).netQuantity +",";

        String tag = "";
        if(tagsMap.containsKey(tagKey)) tag = tagsMap.get(tagKey);

        String info = printAttribs.idAndTime+","+symbol+","+printAttribs.tradeAndEvent+","+printAttribs.reason+","+printAttribs.reasonInfo+","+getClosesInfo()+","+premium+","+printAttribs.pro+","+printAttribs.proBN+","+quantities+tag;
        printInfo.print(info);
        printMasterInfo(printAttribs,tag);
    }

    public String getClosesInfo(){
        String price = ohlc.getClose()+"",eventTriggerPrice = ohlc.eventClose+"";
        return price+","+eventTriggerPrice+","+ltp;
    }

    public String getTag(String tagKey){
        String tag = "";
        if(tagsMap.containsKey(tagKey)) tag = tagsMap.get(tagKey);
        return tag;
    }

    public String getQUTag(String tagKey){
        try {
            String tag = "";
            if (tagsMap.containsKey(tagKey)) tag = tagsMap.get(tagKey);
            tag = tag.substring(0,12)+"888"+tag.substring(15);
            return tag;
        }catch (Exception e){e.printStackTrace();}
        return "qu";
    }

    public void printMasterInfo(PrintAttribs pa,String tag){
        String date = sdf.format(new Date());

        String masterInfo = ValarTrade.strategy+","+pa.ksId+","+pa.tradeId+","+date+","+pa.time+","+symbol+","+pa.transactionType+","+pa.reason+","+getClosesInfo()+","+tag;
        try(BufferedWriter writer = Files.newBufferedWriter(ValarTrade.masterInfoPath, StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)){
            writer.write(masterInfo);
            writer.newLine();

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updatePremium(double indexClose){
        if (peOrCe.equalsIgnoreCase("ce")) {
            if (strike >= indexClose) {
                premium = ohlc.getClose();
            } else {
                premium = ohlc.getClose() - (indexClose - strike);
            }
        } else {
            if (strike > indexClose) {
                premium = ohlc.getClose() - (strike - indexClose);
            } else {
                premium = ohlc.getClose();
            }
        }
    }

    public String getSymbol(){
        return symbol;
    }

    public double getLtp(){
        return ltp;
    }

    public double getEventPrice(){
        return ohlc.eventClose;
    }

    public long getToken(){
        return token;
    }

    public long getSymphonyToken(){
        return symphonyToken;
    }

    public boolean isTraded(){
        return traded;
    }

    public void setAsTraded(){
        this.traded = true;
    }

    public String getPeOrCe(){
        return peOrCe;
    }

    public void setLtp(double ltp){
        this.ltp = ltp;
    }

    public void setOhlc(Ohlc ohlc2){
        ohlc.setValues(ohlc2);
    }

    public Ohlc getOhlc(){
        return ohlc;
    }

    public double getClose(){
        return ohlc.getClose();
    }

    public Object clone()throws CloneNotSupportedException{
        return super.clone();
    }

    public String toString(){
        return "{ symbol "+symbol+" close "+ohlc.getClose()+" }";
    }
}
