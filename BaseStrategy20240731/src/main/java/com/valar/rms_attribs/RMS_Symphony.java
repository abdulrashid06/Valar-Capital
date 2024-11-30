package com.valar.rms_attribs;

import com.valar.accountAttributes.SymphonyAccountAttributes;
import com.valar.application.ValarTrade;
import com.valar.states.State;
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
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.valar.application.ValarTrade.addAPILog;
import static com.valar.application.ValarTrade.allAccountAttributes;

public class RMS_Symphony {
    private Map<String,RMSAttribs> rmsAttribsMap = new HashMap<>();
    private int checkCount = 40;
    private List<String> failedStatus = Arrays.asList("Rejected","Cancelled");
    private List<String> successStatus = Arrays.asList("Filled");

    private State item;
    private ValarTrade valarTrade;
    private String clientID;
    private int id;
    private SymphonyAccountAttributes accountAttributes;
    public RMS_Symphony(ValarTrade valarTrade, int id, String clientID, Map<String,RMSAttribs> rmsAttribsMap, State item, SymphonyAccountAttributes aa){
        try {
            this.rmsAttribsMap = rmsAttribsMap;
            this.id = id;
            this.clientID = clientID;
            this.valarTrade = valarTrade;
            this.item = item;
            this.accountAttributes = aa;


            if(rmsAttribsMap.size() == 0) return;

            int delay;
            for(int i = 1;i <= checkCount;i++){
                delay = i * 3;
                valarTrade.scheduler.schedule(() -> valarTrade.executorService.submit(() -> {
                    if(rmsAttribsMap.size() == 0) return;
                    checkOrders();
                }),delay,TimeUnit.SECONDS);
            }
        }catch (Exception e){
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            String info = LocalTime.now()+" RMSError ClientID "+clientID+" symbol "+item.getSymbol()+" "+exceptionAsString + "," + e.getCause() + "," + e.toString() + "," + e.getMessage() + "," + e.getStackTrace() + "," + e.getLocalizedMessage();
            valarTrade.printError(info);
        }
    }

    private void checkOrders(){
        List<String> toBeRemoved = new ArrayList();

        rmsAttribsMap.entrySet().forEach(ks->{
            String key = ks.getKey();
            RMSAttribs rmsAttribs = ks.getValue();

            Object[] response = getOrderStatus(rmsAttribs.orderId);
            String orderStatus = response[0].toString();
            JSONObject orderResponse = null;
            if(response[1]!=null){
                orderResponse = (JSONObject) response[1];
            }
//            System.out.println("OrderStatus for orderID "+rmsAttribs.orderID+" is "+orderStatus);
            if(failedStatus.contains(orderStatus)){
                if(orderResponse!=null){
                   try{String rejectedMessage = allAccountAttributes.get(id).accountName+","+orderResponse.get("OrderGeneratedDateTime")+","+
                            item.getSymbol()+","+orderResponse.get("CancelRejectReason");
                    try (BufferedWriter writer = Files.newBufferedWriter(valarTrade.rejectedOrdersInfoPath, StandardOpenOption.CREATE,
                            StandardOpenOption.APPEND)) {
                        writer.write(rejectedMessage);
                        writer.newLine();
                    } catch (IOException e) { e.printStackTrace(); }}catch (Exception e){valarTrade.printError(e.toString());}
                }
//                System.out.println(LocalTime.now()+" Order is Incomplete with orderID "+rmsAttribs.orderID);
                if(rmsAttribs.belongsToLimit) {
                    rmsAttribs.getUpdatedBody(item.getLtp());
                }
                rmsAttribs.orderId = placeOrderAgain(rmsAttribs.apiInfo,rmsAttribs.body);
//                System.out.println(rmsAttribs.orderID+" and status is "+orderStatus);
//                System.out.println("RMS Order is placed for " + tradingsymbol+" and orderID "+rmsAttribs.orderID);
            }else if(successStatus.contains(orderStatus)){
                toBeRemoved.add(key);
//                System.out.println(LocalTime.now()+" Order is completed with orderID "+rmsAttribs.orderID);
            }
        });

//        System.out.println(LocalTime.now()+" Checking status Done Size B4 Removing completedOrders "+bAndOMap.size());

        for(String op:toBeRemoved){
//            System.out.println(LocalTime.now()+" Removing completed order with order id "+op.orderID);
            rmsAttribsMap.remove(op);
        }

//        System.out.println(LocalTime.now()+" Checking status Done Size B4 After completedOrders "+bAndOMap.size());
    }

//    private String getOrderStatus(String appOrderId, String logbookResponse) {
//
//        JSONArray jsonResponse=(new JSONObject(logbookResponse)).getJSONArray("result");
//        for(int i=0;i<jsonResponse.length();i++) {
//            JSONObject jsonObject = (JSONObject)jsonResponse.get(i);
//            if(appOrderId.equals(jsonObject.get("AppOrderID").toString())){
//                return jsonObject.get("OrderStatus").toString();
//            }
//        }
//
//        return null;
//    }

    private String placeOrderAgain(ApiInfo apiInfo,String body){
        String appOrderID=null;
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            String request = accountAttributes.url + "/orders?clientID="+clientID;
            HttpPost postRequest = new HttpPost(request);

            StringEntity input = new StringEntity(body);
            postRequest.setHeader("Content-Type", "application/json");
            //put token id of interactive
            postRequest.setHeader("authorization", accountAttributes.interactiveToken);
            postRequest.setEntity(input);
            HttpResponse response = httpClient.execute(postRequest);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));
            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println("Response -> " + output+" "+ LocalTime.now());
                if (output.contains("success")) {
                    appOrderID = (new JSONObject(output)).getJSONObject("result").get("AppOrderID").toString();
                    apiInfo.update(appOrderID);
                    addAPILog(apiInfo);
                }
            }
            httpClient.getConnectionManager().shutdown();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return appOrderID;
    }

    public Object[] getOrderStatus(String appOrderID){
        String response = getResponse(appOrderID);

        if(response==null)return new Object[]{"Rejected",null};
        JSONArray jsonResponse=(new JSONObject(response)).getJSONArray("result");
        if(jsonResponse.length()!=0) {
            JSONObject jsonObject = (JSONObject)jsonResponse.get(jsonResponse.length()-1);
            if(appOrderID.equals(jsonObject.get("AppOrderID").toString())){
                String status = jsonObject.get("OrderStatus").toString();
                ApiInfo apiInfo = new ApiInfo(accountAttributes.accountName,appOrderID);
                addAPILog(apiInfo);
                return new Object[]{status,jsonObject};
            }
        }
        return new Object[]{"Rejected",null};
    }

    public String getResponse(String appOrderID){
        String orderBookResponse = null;
        try {
            String request = accountAttributes.url + "/orders?clientID="+clientID+"&appOrderID="+appOrderID;
            System.out.println("\t\tRequest "+request);
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet getRequest = new HttpGet(request);
            getRequest.setHeader("Content-Type", "application/json");
            getRequest.setHeader("authorization", accountAttributes.interactiveToken);
            HttpResponse response = httpClient.execute(getRequest);


            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));
            orderBookResponse = br.readLine();
            System.out.println(orderBookResponse+" OrderBook Output from Server .... \n"+orderBookResponse+" "+ LocalTime.now());
            httpClient.getConnectionManager().shutdown();

        } catch (MalformedURLException e) {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            valarTrade.printError(LocalTime.now()+" RMS "+exceptionAsString + "," + e.getCause() + "," + e.toString() + "," + e.getMessage() + "," + e.getStackTrace() + "," + e.getLocalizedMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return orderBookResponse;
    }

}