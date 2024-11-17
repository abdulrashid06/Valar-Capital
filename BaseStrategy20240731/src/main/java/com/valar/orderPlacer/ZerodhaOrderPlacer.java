package com.valar.orderPlacer;

import com.valar.accountAttributes.AccountAttributes;
import com.valar.application.ValarTrade;
import com.valar.states.State;
import com.valar.entities.TradeEntity;
import com.valar.rms_attribs.RMS;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.kiteconnect.utils.Constants;
import com.zerodhatech.models.Order;
import com.zerodhatech.models.OrderParams;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.valar.application.ValarTrade.limitRange;
import static com.valar.application.ValarTrade.quantityFreezeMap;

public class ZerodhaOrderPlacer extends OrderPlacer{
    private KiteConnect kiteConnect;
    public ZerodhaOrderPlacer(AccountAttributes aa,int sno,int id, ValarTrade valarTrade, KiteConnect kiteConnect){
        super(aa,sno,id,valarTrade);
        this.kiteConnect = kiteConnect;
    }

    public void placeMarketOrder(int indexType,String tag,QuantAttribs qa,boolean quantityReloaded, State item, boolean isSell, int quan, int id, boolean isSplit, Map ordersMap,boolean isQuantityUpdated,boolean isNormalOrder){
        boolean slExit = false;
        if(ordersMap==null){
            ordersMap = new HashMap();
            slExit = true;
        }

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
        if(quantity==0 || isOrderPlacingOff())return;

        for(int i=0;i<loop;i++) {

            if(!isSplit && i==(loop-1) && lastQuant!=0)
                quantity = lastQuant;

            System.out.println(accountAttributes.accountName+" split "+id+ " Order for " + item.getToken() + " at " + LocalTime.now() + " with quantity " + quantity + " symbol " + item.getSymbol());
            //..............................................Placing the order......................................
            OrderParams orderParams = new OrderParams();

            String tradingSymbol = item.getSymbol();
            System.out.println("Order is about to be placed...for " + tradingSymbol);


            orderParams.product = Constants.PRODUCT_NRML;
            orderParams.quantity = quantity;
            orderParams.tradingsymbol = tradingSymbol;
            if(indexType==3 || indexType==4)orderParams.exchange = Constants.EXCHANGE_BFO;
            else orderParams.exchange = Constants.EXCHANGE_NFO;
            orderParams.orderType = Constants.ORDER_TYPE_LIMIT;
            orderParams.price = limitRange.getLimitPrice(false,isSell,item.getLtp());
            orderParams.validity = Constants.VALIDITY_IOC;

            if (isSell) orderParams.transactionType = Constants.TRANSACTION_TYPE_SELL;
            else orderParams.transactionType = Constants.TRANSACTION_TYPE_BUY;

            orderParams.tag = tag;

            Order order = null;
            try {
                order = kiteConnect.placeOrder(orderParams, Constants.VARIETY_REGULAR);
                System.out.println(order.orderId);
                System.out.println("Order is placed for " + tradingSymbol);
            } catch (KiteException kiteException) {
                onKiteException(valarTrade.errorPath,kiteException);
            } catch (IOException e) {
                onException(valarTrade.errorPath,e);
                e.printStackTrace();
            }

            ordersMap.put(orderParams,order);
            if(slExit)new RMS(this.id,valarTrade,kiteConnect,ordersMap,valarTrade.errorPath,item);
        }
    }

    public static void onKiteException(Path errorInfoPath, KiteException kiteException){
        try (BufferedWriter writer = Files.newBufferedWriter(errorInfoPath, StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            writer.write(LocalTime.now() + "," + kiteException.message + "," + kiteException.toString() + "," + kiteException.getStackTrace());
            writer.newLine();
        } catch (IOException e) {
        }
    }

    public static void onException(Path errorInfoPath,Exception e){
        try (BufferedWriter writer = Files.newBufferedWriter(errorInfoPath, StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            writer.write(LocalTime.now() + "," + e.toString() + "," + e.getMessage());
            writer.newLine();
        } catch (IOException ee) {}
    }

    boolean isNormalOrder;
    public void scheduleLimitOrdersSplits(int indexType,String ids,char tradeType, boolean isShifted, TradeEntity te, State item, boolean isEntry, boolean isQuantityUpdated){
        /*String tag;
        if(isQuantityUpdated)tag = item.getQUTag(te.getTagKey());
        else tag = item.getTag(te.getTagKey());

        boolean isSell;
        if(isEntry){
            if(tradeType=='b')isSell = false; else isSell = true;
        }else{ if(tradeType=='b')isSell = true;else isSell = false; }

        try {
            QuantAttribs qa = updateQuantityAttribs(indexType,isShifted && !isEntry,item,isQuantityUpdated);
            if (isOrderPlacingOff() || qa.totalQuantity==0)
                return;

            isNormalOrder = false;
            scheduleMarketOrder(indexType,tag, item, isSell, isQuantityUpdated, qa, isNormalOrder);

        }catch (Exception e){ onException(valarTrade.errorPath,e); }*/
    }

    public void scheduleMarketOrder(int indexType,String tag, State item, boolean isSell, boolean isQuantityUpdated, QuantAttribs qa, boolean isNormalOrder){
        Map<OrderParams,Order> ordersMap = new HashMap();

        int quan,delay;
        for(int i = 1;i <= qa.times;i++){
            quan = qa.splitQuantity;
            if(i==qa.times && qa.quantAtLast!=0)
                quan = qa.quantAtLast;
            delay = (i-1)*2;
            AtomicInteger ai = new AtomicInteger(i),
                    aiQuan = new AtomicInteger(quan);
            scheduler.schedule(() -> executorService.submit(() -> {
                placeMarketOrder(indexType,tag,qa,true,item,isSell,aiQuan.get(),ai.get(),true,ordersMap,isQuantityUpdated,isNormalOrder);
                if(!isOrderPlacingOff() && ai.get()==qa.times) new RMS(this.id,valarTrade,kiteConnect,ordersMap,valarTrade.errorPath,item);
            }),delay,TimeUnit.SECONDS);
        }
    }

    public void cancelSLM(List<String> orderIDList){
        for(String orderID:orderIDList)
            try{ kiteConnect.cancelOrder(orderID, Constants.VARIETY_REGULAR); } catch (KiteException ke) {onKiteException(valarTrade.errorPath,ke);ke.printStackTrace();
            }  catch (IOException e) { onException(valarTrade.errorPath,e);e.printStackTrace(); }
        orderIDList.clear();
    }
}
