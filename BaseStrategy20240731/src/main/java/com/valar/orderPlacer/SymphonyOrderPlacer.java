package com.valar.orderPlacer;

import com.valar.accountAttributes.AccountAttributes;
import com.valar.accountAttributes.SymphonyAccountAttributes;
import com.valar.application.ValarTrade;
import com.valar.states.State;
import com.valar.entities.TradeEntity;
import com.valar.rms_attribs.RMSAttribs;
import com.valar.rms_attribs.RMS_Symphony;
import com.valar.utils.ApiInfo;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.valar.application.ValarTrade.*;

public class SymphonyOrderPlacer extends OrderPlacer{
    private String clientID;
    private Map<String, List<String>> orderIDsMap = new HashMap();
    public SymphonyOrderPlacer(AccountAttributes aa, int sno, int id, ValarTrade valarTrade, String clientID){
        super(aa,sno,id,valarTrade);
        this.clientID = clientID;
        this.valarTrade = valarTrade;
    }

    private void placeMarketOrder(int indexType, String tag, boolean isSell, State item, int quantity, Map<String, RMSAttribs> bodyAndOrderIDMap, boolean isQuantityUpdated, boolean isNormalOrder){
        boolean slExit = false;
        if(bodyAndOrderIDMap==null){
            bodyAndOrderIDMap = new HashMap();
            slExit = true;
        }

        String body=null,appOrderID=null;

        String tradingSymbol = item.getSymbol();
        System.out.println("Order is about to be placed...for "+tradingSymbol+" with quantity "+quantity);

        if(quantity==0 || isOrderPlacingOff())return;

        double ltp = item.getLtp(),
                limitPrice = limitRange.getLimitPrice(false,isSell,ltp);
        String limitPriceLn = "  \"limitPrice\": "+limitPrice+",\n";;
        ApiInfo apiInfo = null;
        try {
            String bOrS;

            if(isSell)
                bOrS = "SELL";
            else
                bOrS = "BUY";


            DefaultHttpClient httpClient = new DefaultHttpClient();
            String request = ((SymphonyAccountAttributes)accountAttributes).url+"/orders";
            HttpPost postRequest = new HttpPost(request);

            String exchangeSegment;
            if(indexType==3 || indexType==4)exchangeSegment = "  \"exchangeSegment\": \"BSEFO\",\n" ;
            else exchangeSegment = "  \"exchangeSegment\": \"NSEFO\",\n" ;

            body = "{\n" +
                    "\"clientID\": \""+clientID+"\",\n" +
                    exchangeSegment +
                    "  \"exchangeInstrumentID\": "+item.getSymphonyToken()+",\n" +
                    "  \"orderType\": \"LIMIT\",\n" +
                    limitPriceLn +
                    "  \"timeInForce\": \"IOC\",\n"+
                    "  \"productType\": \"NRML\",\n" +
                    "  \"orderSide\": \""+bOrS+"\",\n" +
                    "  \"disclosedQuantity\": "+0+",\n" +
                    "  \"orderQuantity\": "+quantity+",\n" +
                    "  \"stopPrice\": "+0+",\n" +
                    "  \"orderUniqueIdentifier\": \""+tag+"\"\n" +
                    "}";
            StringEntity input = new StringEntity(body);

            System.out.println(body);
            postRequest.setHeader("Content-Type", "application/json");
            //put token id of interactive
            postRequest.setHeader("authorization", ((SymphonyAccountAttributes)accountAttributes).interactiveToken);
            postRequest.setEntity(input);
            HttpResponse response = httpClient.execute(postRequest);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));
            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println("Response -> "+output);
                if (output.contains("success")) {
                    appOrderID = (new JSONObject(output)).getJSONObject("result").get("AppOrderID").toString();
                    apiInfo = new ApiInfo(new String[]{accountAttributes.accountName,appOrderID},
                            new String[]{tag,tradingSymbol},bOrS,ltp,limitPrice,quantity);
                    addAPILog(apiInfo);
                }
            }
            httpClient.getConnectionManager().shutdown();

        } catch (MalformedURLException e) {
            printError(e);
            e.printStackTrace();
        } catch (IOException e) {
            printError(e);
            e.printStackTrace();}

        if(body!=null && appOrderID!=null)bodyAndOrderIDMap.put(appOrderID,new RMSAttribs(apiInfo,true,isSell,appOrderID,body,limitPriceLn));
        if(slExit)new RMS_Symphony(valarTrade,id,clientID,bodyAndOrderIDMap,item,((SymphonyAccountAttributes)accountAttributes));
    }


    private void printError(Exception e){
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();
        try (BufferedWriter writer = Files.newBufferedWriter(valarTrade.errorPath, StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            writer.write(LocalTime.now()+","+exceptionAsString + "," + e.getCause() + "," + e.toString() + "," + e.getMessage() + "," + e.getStackTrace() + "," + e.getLocalizedMessage());
            writer.newLine();
        } catch (IOException ee) {
            e.printStackTrace();
        }
    }

    public void placeMarketOrder(int indexType,String tag,QuantAttribs qa,boolean quantityReloaded,State item,boolean isSell,int quan,int id,boolean isSplit,Map bodyAndOrderIDMap,boolean isQuantityUpdated,boolean isNormalOrder){

        if(!quantityReloaded) qa = updateQuantityAttribs(indexType,false,item,false);

        int quantity,loop = 1,lastQuant=0;
        if(!isSplit){   //for stoplosses
            int splitQuantity = quantityFreezeMap.get(indexType);
            int[] res = getQuantity(qa.totalQuantity,splitQuantity);
            loop = res[0];lastQuant=res[1];
            quantity = splitQuantity;
        }else {
            quantity = quan;
        }


        for(int i=0;i<loop;i++) {
            if(!isSplit && i==(loop-1) && lastQuant!=0)
                quantity = lastQuant;
            System.out.println(id+" Order for "+item.getSymphonyToken()+" at "+ LocalTime.now()+" with quantity "+quantity+" symbol "+item.getSymbol());
            placeMarketOrder(indexType,tag,isSell,item,quantity,bodyAndOrderIDMap,isQuantityUpdated,isNormalOrder);
        }

    }

    boolean isNormalOrder;
    public void scheduleLimitOrdersSplits(int indexType,String ids,char tradeType, boolean isShifted, TradeEntity te, State item, boolean isEntry, boolean isQuantityUpdated){
//        String tag;
//        if(isQuantityUpdated)tag = item.getQUTag(te.getTagKey());
//        else tag = item.getTag(te.getTagKey());
//
//        boolean isSell;
//        if(isEntry){ if(tradeType=='b')isSell = false; else isSell = true;
//        }else{ if(tradeType=='b')isSell = true;else isSell = false; }
//
//        try {
//            QuantAttribs qa = updateQuantityAttribs(indexType,isShifted && !isEntry,item,isQuantityUpdated);
//            if (isOrderPlacingOff() || qa.totalQuantity==0)
//                return;
//
//            isNormalOrder = false;
//            scheduleMarketOrder(indexType,tag,item,isSell,isQuantityUpdated,qa,isNormalOrder);
//
//        }catch (Exception e){ printError(e); }
    }

    public void scheduleMarketOrder(int indexType,String tag,State item,boolean isSell,boolean isQuantityUpdated,QuantAttribs qa, boolean isNormalOrder){
        Map<String,RMSAttribs> bodyAndOrderIDMap = new HashMap();

        int quan,delay;
        for(int i = 1;i <= qa.times;i++){
            quan = qa.splitQuantity;
            if(i==qa.times && qa.quantAtLast!=0)
                quan = qa.quantAtLast;
            delay = (i-1)*2;
            AtomicInteger ai = new AtomicInteger(i),
                    aiQuan = new AtomicInteger(quan);
            scheduler.schedule(() -> executorService.submit(() -> {
                placeMarketOrder(indexType,tag,qa,true,item,isSell,aiQuan.get(),ai.get(),true,bodyAndOrderIDMap,isQuantityUpdated,isNormalOrder);
                if (!isOrderPlacingOff() && ai.get()==qa.times)new RMS_Symphony(valarTrade,id,clientID,bodyAndOrderIDMap,item,((SymphonyAccountAttributes)accountAttributes));
            }),delay,TimeUnit.SECONDS);
        }
    }


    private String getOrderBookResult(String appOrderID){
        String output = null;
        if(isOrderPlacingOff())return "";
        try {
            String request = ((SymphonyAccountAttributes)accountAttributes).url + "/orders?clientID="+clientID+"&appOrderID="+appOrderID;
            System.out.println("\t\tRequest "+request);
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet getRequest = new HttpGet(request);
            getRequest.setHeader("Content-Type", "application/json");
            getRequest.setHeader("authorization", ((SymphonyAccountAttributes)accountAttributes).interactiveToken);
            HttpResponse response = httpClient.execute(getRequest);


            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));
            output = br.readLine();
            System.out.println("Output "+output);
            httpClient.getConnectionManager().shutdown();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }

    private boolean getOrderStatus(String appOrderId) {
        try {
            String logbookResponse = getOrderBookResult(appOrderId);

            JSONArray jsonResponse = (new JSONObject(logbookResponse)).getJSONArray("result");
            JSONObject jsonObject = (JSONObject) jsonResponse.get(jsonResponse.length() - 1);

            if (jsonObject != null)
                return jsonObject.get("OrderStatus").toString().equals("Filled");
        }catch (Exception e){ e.printStackTrace();}

        return false;
    }
}
