package capital.valar.supertrend.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AdditionalOverallMetrics {

    Map<String, Attribs> optionsDateAttribs;
    private String dateFormat = "dd-MM-yy";
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
    float maxDrawDownPoints = -Float.MAX_VALUE,maxDrawDownPercentage = -Float.MAX_VALUE;

    private class AvgRelatedMetrices{
        float netProfitInProfitableDays,netLossInLosableDays;
        float daysInProfits,daysInLoss;
        float netAvgProfitOrProfitPercent, netAvgLossOrLossPercent;
        float overAllNetProfitOrProfitPercent;

        public void update(float netProfitOrProfitPercent){
            if(netProfitOrProfitPercent>=0){
                netProfitInProfitableDays += netProfitOrProfitPercent;
                daysInProfits++;
            } else{
                netLossInLosableDays += netProfitOrProfitPercent;
                daysInLoss++;
            }
            overAllNetProfitOrProfitPercent += netProfitOrProfitPercent;
        }

        public void compute(){
            netAvgProfitOrProfitPercent = netProfitInProfitableDays/daysInProfits;
            netAvgLossOrLossPercent = netLossInLosableDays/daysInLoss;
        }
    }

    public AdditionalOverallMetrics(Map<String,Attribs> optionsDateAttribs){
        this.optionsDateAttribs = new TreeMap<>(optionsDateAttribs);
    }
    private AvgRelatedMetrices netProfitPercentRelatedMetrices = new AvgRelatedMetrices(),
    netProfitRelatedMetrices = new AvgRelatedMetrices();

    class MonthAttrib{
        float profit,profitPercent;
        public MonthAttrib(float profit,float profitPercent){
            this.profit = profit;
            this.profitPercent = profitPercent;
        }

        public void add(float profit,float profitPercent){
            this.profit += profit;
            this.profitPercent += profitPercent;
        }

        @Override
        public String toString() {
            return "{ Profit "+profit+" , profitPercent "+profitPercent+" }";
        }
    }

    public Map<String,Float> getAdditionalMetrics(){
        String date,monthMapKey;
        Attribs tradeAttrib;
        float bnCloseAtDayStart,netProfit,netProfitPercent;
        Map<String,MonthAttrib> monthAttribMap = new TreeMap<>();

        List<LocalDate> datelist=new ArrayList();
        for(String key : optionsDateAttribs.keySet()){
            LocalDate localDate = LocalDate.parse(key,formatter);
            datelist.add(localDate);
        }

        Collections.sort(datelist);

        float maxCumulativeProfit = -Float.MAX_VALUE,maxCumulativeProfitPercent = -Float.MAX_VALUE,drawDown,drawDownPercent;
        float cumulativeNetProfit = 0,cumulativeNetProfitPercent = 0;
        for(LocalDate ld : datelist){
            date = formatter.format(ld);tradeAttrib = optionsDateAttribs.get(date);
            bnCloseAtDayStart = optionsDateAttribs.get(date).bnCloseAtEntry;
            netProfit = tradeAttrib.profitWithCost;
            netProfitPercent = (netProfit/bnCloseAtDayStart) * 100;

            monthMapKey = date.substring(3);
            if(monthAttribMap.containsKey(monthMapKey))monthAttribMap.get(monthMapKey).add(netProfit,netProfitPercent);
            else monthAttribMap.put(monthMapKey,new MonthAttrib(netProfit,netProfitPercent));

            cumulativeNetProfit += netProfit;
            cumulativeNetProfitPercent += netProfitPercent;

            maxCumulativeProfit = Float.max(maxCumulativeProfit,cumulativeNetProfit);
            maxCumulativeProfitPercent = Float.max(maxCumulativeProfitPercent,cumulativeNetProfitPercent);

            drawDown = maxCumulativeProfit - cumulativeNetProfit;
            drawDownPercent = maxCumulativeProfitPercent - cumulativeNetProfitPercent;

            maxDrawDownPoints = Float.max(maxDrawDownPoints,drawDown);
            maxDrawDownPercentage = Float.max(maxDrawDownPercentage,drawDownPercent);

            netProfitPercentRelatedMetrices.update(netProfitPercent);
            netProfitRelatedMetrices.update(netProfit);
        }

        netProfitPercentRelatedMetrices.compute();
        netProfitRelatedMetrices.compute();

        float totalMonthsInProfit = 0;
        for(Map.Entry<String,MonthAttrib> entry : monthAttribMap.entrySet()){
            if(entry.getValue().profitPercent>=0)totalMonthsInProfit++;
        }
        float monthWinPercent = (totalMonthsInProfit/(float)monthAttribMap.size())*100f,
        calmar = (netProfitPercentRelatedMetrices.overAllNetProfitOrProfitPercent /maxDrawDownPercentage);


        return new HashMap(){
            {
                put("NetProfit",netProfitRelatedMetrices.overAllNetProfitOrProfitPercent);
                put("NetAvgProfit",netProfitRelatedMetrices.netAvgProfitOrProfitPercent);
                put("NetAvgLoss",netProfitRelatedMetrices.netAvgLossOrLossPercent);
                put("NetProfitPercent",netProfitPercentRelatedMetrices.overAllNetProfitOrProfitPercent);
                put("NetAvgProfitPercent",netProfitPercentRelatedMetrices.netAvgProfitOrProfitPercent);
                put("NetAvgLossPercent",netProfitPercentRelatedMetrices.netAvgLossOrLossPercent);
                put("MonthWinPercent",monthWinPercent);
                put("MaxDrawDown",maxDrawDownPoints);
                put("MaxDrawDownPercent",maxDrawDownPercentage);
                put("Calmar",calmar);
            }
        };
    }


}
