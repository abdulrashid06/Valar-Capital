package com.valar.rms_attribs;

import com.valar.application.ValarTrade;
import com.valar.states.State;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.kiteconnect.utils.Constants;
import com.zerodhatech.models.Order;
import com.zerodhatech.models.OrderParams;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.valar.application.ValarTrade.allAccountAttributes;
import static com.valar.application.ValarTrade.limitRange;

public class RMS {
    private Map<OrderParams, Order> ordersMap;
    private int checkCount = 40;
    private List<String> failedStatus = Arrays.asList("CANCELLED","REJECTED");
    private List<String> successStatus = Arrays.asList("COMPLETE");
    private KiteConnect kiteConnect;

    private List<Order> orders;
    private State item;
    private Path errorInfoPath;
    private ValarTrade valarTrade;
    private int accountID;
    public RMS(int accountID, ValarTrade valarTrade, KiteConnect kiteConnect, Map<OrderParams,Order> ordersMap, Path errorInfoPath, State item){
        try {
            this.kiteConnect = kiteConnect;
            this.ordersMap = ordersMap;
            this.errorInfoPath = errorInfoPath;
            this.valarTrade = valarTrade;
            this.item = item;
            this.accountID = accountID;

            if(ordersMap.size() == 0) return;

            int delay;
            for(int i = 1;i <= checkCount;i++){
                delay = i * 3;
                valarTrade.scheduler.schedule(() -> valarTrade.executorService.submit(() -> {
                    if(ordersMap.size() == 0) return;
                    try {orders = kiteConnect.getOrders();
                    } catch (KiteException e) {onKiteError(e);} catch (Exception e) {onError(e);}
                    checkOrders();
                }),delay,TimeUnit.SECONDS);
            }
        }catch (Exception e){
            onError(e);
        }
    }

    public void onError(Exception e){
        e.printStackTrace();
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();
        String info = LocalTime.now()+" ErrorRMS accountID  "+accountID+" "+exceptionAsString + "," + e.getCause() + "," + e.toString() + "," + e.getMessage() + "," + e.getStackTrace() + "," + e.getLocalizedMessage();
        valarTrade.printError(info);
    }

    public void onKiteError(KiteException e){
        e.printStackTrace();
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();
        String info = LocalTime.now()+" KiteErrorRMS accountID "+accountID+" "+exceptionAsString + "," + e.getCause() + "," + e.toString() + "," + e.getMessage() + "," + e.getStackTrace() + "," + e.getLocalizedMessage();
        valarTrade.printError(info);
    }

    class Count{
        public int c;
    }

    public void checkOrders(){
        List<OrderParams> toBeRemoved = new ArrayList();
        ordersMap.keySet().forEach(op->{
            Order order = ordersMap.get(op);

            order = getStatus(orders,order.orderId);
            String orderStatus = order.status;
//            System.out.println("OrderStatus for orderID "+order.orderId+" is "+orderStatus);


            if(failedStatus.contains(orderStatus)){
                try{String rejectedMessage = allAccountAttributes.get(accountID).accountName+","+order.orderTimestamp+","+
                        item.getSymbol()+","+order.statusMessage.split(",")[0];
                try (BufferedWriter writer = Files.newBufferedWriter(valarTrade.rejectedOrdersInfoPath, StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND)) {
                    writer.write(rejectedMessage);
                    writer.newLine();
                } catch (IOException e) { e.printStackTrace(); }}catch (Exception e){onError(e);}

                try {
//                    System.out.println(LocalTime.now()+" Order is Incompleted with orderID "+order.orderId+" and price "+op.price);
                    if(op.orderType == Constants.ORDER_TYPE_LIMIT) {
                        op.price = limitRange.getLimitPrice(false,op.transactionType == Constants.TRANSACTION_TYPE_SELL, item.getLtp());
                    }
                    order = kiteConnect.placeOrder(op, Constants.VARIETY_REGULAR);
//                    System.out.println(order.orderId+" and status is "+orderStatus);
//                    System.out.println("RMS Order is placed for " + op.tradingsymbol+" and orderID "+order.orderId);
                } catch (KiteException kiteException) {
                    onKiteError(kiteException);
                    kiteException.printStackTrace();}catch (Exception exception){exception.printStackTrace();onError(exception); }
                ordersMap.replace(op,order);
            }else if(successStatus.contains(orderStatus)){

                toBeRemoved.add(op);
//                System.out.println(LocalTime.now()+" Order is completed with orderID "+order.orderId);
            }
        });

//        System.out.println(LocalTime.now()+" Checking status Done Size B4 Removing completedOrders "+ordersMap.size());

        for(OrderParams op:toBeRemoved){
//            System.out.println(LocalTime.now()+" Removing completed order with order id "+ordersMap.get(op).orderId);
            ordersMap.remove(op);}

//        System.out.println(LocalTime.now()+" Checking status Done Size B4 After completedOrders "+ordersMap.size());
    }

    public Order getStatus(List<Order> orders,String orderID){
        for(Order order:orders)
            if(order.orderId.equals(orderID))
                return order;

        return null;
    }

    public static String getStatus(KiteConnect kiteConnect,String orderID){
        try {
            List<Order> orders = kiteConnect.getOrders();

            for (Order order : orders)
                if (order.orderId.equals(orderID))
                    return order.status;
        }catch (KiteException ke){}catch (Exception e){}

        return null;
    }
}
