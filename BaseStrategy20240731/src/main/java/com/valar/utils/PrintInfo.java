package com.valar.utils;

import com.valar.accountAttributes.AccountAttributes;
import com.valar.application.ValarTrade;
import com.valar.indices.Index;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static com.valar.application.ValarTrade.allAccountAttributes;

public class PrintInfo {

    private Path path;
    private ValarTrade valarTrade;
    public PrintInfo(ValarTrade valarTrade,Path path){
        this.path = path;
        this.valarTrade = valarTrade;
        try(BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            writer.write("KeyStoreID,TradeId,Time,Option,TradeType,Event,Reason,ReasonInfo,Close,EventTriggerPrice,OrderPrice,Premium,Profit,ProfitPercent,"+getQuantitiesHeading()+"Tag,BNClose,NiftyClose,FNClose");
            writer.newLine();
            //ids+","+LocalTime.now()+","+symbol+","+ltp+","+profit+","+profitPercent+","+reason+","+reasonInfo+","+quantities+tag
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getIndexCloses(){
        String indexPrices = "";
        for(Index index : ValarTrade.indexMap.values())
            indexPrices += ","+index.stockState.getOhlc().getClose();

        return indexPrices;
    }

    public void print(String info){
        try(BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)){
            writer.write(info+getIndexCloses());
            writer.newLine();

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getQuantitiesHeading(){
        String heading="";
        for(AccountAttributes accountAttributes:allAccountAttributes)
            heading+="Quantity"+accountAttributes.accountName+",";
        return heading;
    }

}
