package com.valar.indices;

import static com.valar.application.ValarTrade.getInMinutes;
import static com.valar.utils.ValarUtils.getUpdateLinesAccToPeriod;
import static com.valar.utils.ValarUtils.loadSeries;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import com.valar.application.ValarTrade;
import com.valar.entities.BollingerBand;
import com.valar.processors.TickStateProcessor;
import com.valar.processors.TickToMinuteProcessor;
import com.valar.states.IndexState;
import com.valar.states.State;
import com.valar.states.StockState;
import com.valar.utils.KeyValues;

public abstract class Index{
    public String strategy;
    private String instrumentsFutureFile = "instrumentsOPTIDX_Futures.csv";
    public State stockState,futureState;
    public int type;
    public boolean keystorePresent;
    public String symbol;
    public long token;
    public Map<Integer,BarSeries>  minSeriesMap = new HashMap();
    List<String>  indexLines;
    public Map<Integer,Double> dayAtrIndicatorsMap = new HashMap<>();
    private BarSeries daySeries;
    public int strikePlus;
    private String instrumentFile;
    public String minFile,dayFile;
    public Map<String,BollingerBand> bollingerBandMap = new HashMap<>();
    public Map<Integer,Boolean> indicatorUpdatedForPeriodMap = new HashMap<>();

    public HashMap<Long, StockState> stockStates = new HashMap();
    public Index(String symbol,long token,String postFix,int type,int strikePlus){
        this.symbol = symbol;
        this.token = token;
        this.strategy = ValarTrade.strategy+postFix;
        this.instrumentFile = "instrumentsOPTIDX"+postFix+".csv";
        this.type = type;
        this.strikePlus = strikePlus;
        String minOrDayFilePrefix = getMinOrDayFilePrefix();
        this.minFile = minOrDayFilePrefix+" 1min downloaded.csv";
        this.dayFile = minOrDayFilePrefix+" Day downloaded.csv";

        loadIndexes();
    }

    private String getMinOrDayFilePrefix(){
        if(type==0)return "BankNifty";
        else if(type==1)return "Nifty";
        else if(type==2)return "FinNifty";
        else if(type==3)return "Bankex";
        else return "Sensex";
    }
    
    private void loadIndexes(){
        stockState = new IndexState(token, token, symbol);

        List<String> futureLines;
        try{futureLines = Files.readAllLines(Paths.get(instrumentsFutureFile));}catch (Exception e){futureLines = new ArrayList<>();}
        Optional<String> ss = futureLines.stream().filter(s-> s.split(",")[2].startsWith(symbol)).findFirst();
        if(ss.isPresent()) {
            String[] relatedLine = ss.get().split(",");
            futureState = new IndexState(Long.parseLong(relatedLine[0]), Long.parseLong(relatedLine[1]), relatedLine[2]);
        }
    }

    public String getInstrumentFile(){
        return instrumentFile;
    }

    public Object[] getTick2MinAndTickState(ValarTrade valarTrade){
        TickToMinuteProcessor tick2MinuteProcessorBN = new TickToMinuteProcessor(valarTrade,stockState,token,type,new HashSet<>(minSeriesMap.keySet()));
        TickStateProcessor tickStateProcessorBN = new TickStateProcessor(token);

        return new Object[]{tick2MinuteProcessorBN,tickStateProcessorBN};
    }

    public Object[] getTick2MinAndTickStateFuture(ValarTrade valarTrade){
        TickToMinuteProcessor tick2MinuteProcessorBNFuture = new TickToMinuteProcessor(valarTrade,futureState,futureState.getToken(),3);
        TickStateProcessor tickStateProcessorBNFuture = new TickStateProcessor(futureState.getToken());

        return new Object[]{tick2MinuteProcessorBNFuture,tickStateProcessorBNFuture};
    }

    public void addIndicators(KeyValues kv) throws Exception{
        if(indexLines==null){
            indexLines = Files.readAllLines(Paths.get(minFile));
            indexLines = indexLines.subList(indexLines.size() - 15000, indexLines.size());
            appendLines(indexLines, type);
            indexLines = removeLinesAfter15_29(indexLines);
        }
        /*

         */

        BarSeries minSeries;
        if(minSeriesMap.containsKey(kv.candlePeriod))minSeries = minSeriesMap.get(kv.candlePeriod);
        else{
            List<String> lines = indexLines;
            if(kv.candlePeriod!=1)lines = getUpdateLinesAccToPeriod(indexLines, kv.candlePeriod);
            minSeries = loadSeries(lines, true);
            minSeriesMap.put(kv.candlePeriod,minSeries);
        }

        if(daySeries==null){
            List<String> dayLines = Files.readAllLines(Paths.get(dayFile));
            daySeries = loadSeries(dayLines,false);
        }
        
        if(!bollingerBandMap.containsKey(kv.period+":"+kv.sd)) {
        	String key  = kv.period+":"+kv.sd;
            bollingerBandMap.put(key,new BollingerBand(minSeries,kv.period,kv.sd));
        }

        if(!dayAtrIndicatorsMap.containsKey(kv.atrPeriod)){
            ATRIndicator atrIndicator = new ATRIndicator(daySeries,kv.atrPeriod);
            dayAtrIndicatorsMap.put(kv.atrPeriod,atrIndicator.getValue(daySeries.getEndIndex()).doubleValue());
        }
    }

    private void appendLines(List<String> indexLines,int indexType)throws Exception{
        String filePath = ".\\logbooks/IndexInfo.csv";
        int minsAt9_15 = getInMinutes("09:15");
        if (new File(filePath).exists()) {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            String lastLn = indexLines.get(indexLines.size() - 1);
            String[] updatedTimeSplits;
            String updatedTime, lastUpdatedTime = "";

            String[] splits;
            String pricesInfo, associatedPrice;

            String pattern = "dd-MM-yy",
                    dateInString = new SimpleDateFormat(pattern).format(new Date());
            System.out.println(lastLn);

            for (String ln : lines) {
                splits = ln.split(",");
                updatedTimeSplits = splits[0].split(":");
                updatedTime = dateInString + " " + updatedTimeSplits[0] + ":" + updatedTimeSplits[1];
                if (lastUpdatedTime.isEmpty() || !lastUpdatedTime.equals(updatedTime)) {
                    pricesInfo = ln.split("IndexPrices ")[1];
                    if (pricesInfo.contains(",")) pricesInfo = pricesInfo.split(",")[indexType];
                    associatedPrice = pricesInfo.split(":")[indexType];

                    int mins = getInMinutes(splits[0]);
                    if (mins >= minsAt9_15) {
                        String updatedLn = updatedTime+","+associatedPrice.replace(";",",")+",0";
                        indexLines.add(updatedLn);
                    }
                    lastUpdatedTime = updatedTime;
                }
            }
        }
    }

    private List<String> removeLinesAfter15_29(List<String> lines){
        int mins;
        String dnt,time;
        String[] splits;
        List<String> updatedLines = new ArrayList<>();
        float c = 0;
        for(String ln : lines){
            splits = ln.split(",");
            dnt = splits[0];
            time = dnt.split(" ")[1];
            mins = ValarTrade.getInMinutes(time);
            if(mins >= 930)continue;
            c = Float.parseFloat(splits[4]);
            updatedLines.add(ln);
        }
        lines = updatedLines;

        return lines;
    }
    
    
    public Optional<Map<BollingerBand.BBType,Double>> getBollingerBandValues(int candlePeriod,int bbPeriod, int bbSd){
        String key  = bbPeriod+":"+bbSd;
        int index = minSeriesMap.get(candlePeriod).getEndIndex();
        if(bollingerBandMap.containsKey(key)) {
            BollingerBand st = bollingerBandMap.get(key);
            return Optional.of(st.getBbValues(false,index));
        }

        return Optional.empty();
    }
    
    
    public Optional<Map<BollingerBand.BBType,Double>> getBollingerBandValuesAtLb(int candlePeriod,int lbPeriod,int bbPeriod, int bbSd){
        String key  = bbPeriod+":"+bbSd;
        int lbIndex =  minSeriesMap.get(candlePeriod).getEndIndex() - lbPeriod;
        if(bollingerBandMap.containsKey(key) && lbIndex>=0) {
            BollingerBand st = bollingerBandMap.get(key);
            return Optional.of(st.getBbValues(true,lbIndex));
        }

        return Optional.empty();
    }

}
