package capital.valar.supertrend.state.minute;

import capital.valar.supertrend.entities.BollingerBand;
import capital.valar.supertrend.entities.Ohlc;
import capital.valar.supertrend.entities.VWAPEntity;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.DecimalNum;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class State {
    public List<String> lines;
    public int parser;
    public String name,line;
    public Ohlc ohlc = new Ohlc();
    public boolean finished;
    private String dateTimeFormat;
    private float gapPercent;
    private BarSeries series;
    private Map<String,BollingerBand> bollingerBandMap = new HashMap<>();
    public VWAPEntity vwapEntity;

    public State(String name, String path, int parser, String dateTimeFormat,boolean removeDayIfDataNotPresent){
        this.name = name;
        this.parser = parser;
        loadLines(path,removeDayIfDataNotPresent);
        line = lines.get(parser);
        ohlc.update(line);
        this.dateTimeFormat = dateTimeFormat;
        finished = parser >= lines.size();
    }

    public State(String name, String path, int parser, String dateTimeFormat,int period,boolean removeDayIfDataNotPresent){
        this.name = name;
        this.parser = parser;
        this.lines = null;
        loadLines(path,removeDayIfDataNotPresent);
        updateLinesAccToPeriod(period);
        line = lines.get(parser);
        ohlc.update(line);
        this.dateTimeFormat = dateTimeFormat;
        finished = parser >= lines.size();
    }

    public State(String name, String path, String readTill, String dateTimeFormat,int period,boolean removeDayIfDataNotPresent){
        this.name = name;
        loadLines(path,removeDayIfDataNotPresent);
        updateLinesAccToPeriod(period);
        for(parser =0;parser<lines.size();parser++) {
            line = lines.get(parser);
            if(line.startsWith(readTill)) break;
        }
        ohlc.update(line);
        this.dateTimeFormat = dateTimeFormat;
        finished = parser >= lines.size();
    }

    public State(String name, String path,boolean readB4,String readTillOrB4Dnt, String dateTimeFormat,boolean removeDayIfDataNotPresent){
        this.name = name;
        loadLines(path,removeDayIfDataNotPresent);
        String nextLn = null;
        for(parser =0;parser<lines.size();parser++) {
            line = lines.get(parser);
            if((parser+1)<lines.size())nextLn = lines.get(parser+1);
            if(!readB4 && line.startsWith(readTillOrB4Dnt)) break;
            else if(readB4 && nextLn!=null && nextLn.startsWith(readTillOrB4Dnt))break;
        }
        ohlc.update(line);
        this.dateTimeFormat = dateTimeFormat;
    }

    private void loadLines(String path,boolean removeDayIfDataNotPresent){
        try{ this.lines = Files.readAllLines(Paths.get(path)); }catch (Exception e){e.printStackTrace();}
        if(removeDayIfDataNotPresent)removeLinesIfEntireDayDataNotPresent();
    }

    private void removeLinesIfEntireDayDataNotPresent(){
        List<String> updatedLines = new ArrayList<>();
        List<String> dayData = new ArrayList<>();
        String[] splits;boolean nonZeroFound = false;
        String time;
        float price;
        for(String ln : lines){
            splits = ln.split(",");
            time = splits[0].split(" ")[1];
            price = Float.parseFloat(splits[4]);
            if(!nonZeroFound && price!=0)nonZeroFound = true;
            dayData.add(ln);

            if(time.equalsIgnoreCase("15:29")){
                if(nonZeroFound) updatedLines.addAll(dayData);
                dayData.clear();
                nonZeroFound = false;
            }
        }

        lines = updatedLines;
    }


    private void updateLinesAccToPeriod(int period){
        if(period<=1)return;
        float open=0,high = -Float.MAX_VALUE,
                low=Float.MAX_VALUE,close = 0,volume=0;
        String dnt,lastSavedDnt=null;
        int periodCounter = 0;
        String[] splits;
        List<String> updatedLines = new ArrayList<>();
        for(String ln : lines){
            splits = ln.split(",");
            dnt = splits[0];
            float o = Float.parseFloat(splits[1]),
                    h = Float.parseFloat(splits[2]),
                    l = Float.parseFloat(splits[3]),
                    c = Float.parseFloat(splits[4]),v = 0;
            if(splits.length>=6) v = Float.parseFloat(splits[5]);

            if((periodCounter)%period==0 || dnt.contains("09:15")){
                if(lastSavedDnt!=null)
                    updatedLines.add(lastSavedDnt + "," + open + "," + high
                            + "," + low + "," + close + "," + volume);
                lastSavedDnt = dnt;
                open = o;
                high = h;
                low = l;
                close = c;
                volume = v;
                periodCounter = 0;
            }else{
                high = Float.max(high,h);
                low = Float.min(low,l);
                close = c;
                volume+=v;
            }
            periodCounter++;
        }

        updatedLines.add(lastSavedDnt + "," + open + "," + high
                + "," + low + "," + close + "," + volume);

        lines = updatedLines;
    }

    public BarSeries loadSeries(){
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(dateTimeFormat);
        BarSeries series = new BaseBarSeries(name);
        String ln,dnt;
        int i=0;
        for(;i<lines.size();i++) {
            ln = lines.get(i);
            String[] lineSplits = ln.split(",");
            dnt = lineSplits[0];
            ZonedDateTime date = LocalDateTime.parse(dnt, DATE_TIME_FORMATTER).atZone(ZoneId.systemDefault());
            double openPrice = Double.parseDouble(lineSplits[1]);
            double highPrice = Double.parseDouble(lineSplits[2]);
            double lowPrice = Double.parseDouble(lineSplits[3]);
            double closePrice = Double.parseDouble(lineSplits[4]);
            double volume = 0;
            
           
            	try{volume = Double.parseDouble(lineSplits[5]);}catch (Exception e){}
                BaseBar bar = BaseBar.builder(DecimalNum::valueOf, Number.class)
                        .timePeriod(Duration.ofMinutes(1))
                        .endTime(date)
                        .openPrice(openPrice)
                        .highPrice(highPrice)
                        .lowPrice(lowPrice)
                        .closePrice(closePrice)
                        .volume(volume)
                        .build();
                series.addBar(bar);
            
        }
        return series;
    }

    public void loadIndicatorsFromPreviousLines(int readFrom){
        for(int i = Integer.max(0,parser-readFrom);i<=parser;i++){
            line = lines.get(i);
            ohlc.update(line);
            updateIndicators();
        }
    }

    public void readTillDateWhileUpdatingIndicatorsFromLinesB4(String readTill,int updateIndicatorsFrom){
        for(parser = 0;parser<lines.size();parser++) {
            line = lines.get(parser);
            if(line.startsWith(readTill)) break;
        }

        if(parser!=lines.size())
            for (int i = Integer.max(0, parser - updateIndicatorsFrom); i <= parser; i++) {
                line = lines.get(i);
                ohlc.update(line);
                updateIndicators();
            }
    }

    public void readTillDate(boolean startFromBeginning,boolean readB4,String readTillOrB4Dnt){
        if(startFromBeginning)parser=0;
        String nextLn = null;
        for(;parser<lines.size();parser++) {
            line = lines.get(parser);
            ohlc.update(line);
            if((parser+1)<lines.size())nextLn = lines.get(parser+1);
            if(!readB4 && line.startsWith(readTillOrB4Dnt)) break;
            else if(readB4 && nextLn!=null && nextLn.startsWith(readTillOrB4Dnt))break;
        }
    }

    public void readTillDateWhileUpdatingIndicators(boolean startFromBeginning,boolean readB4,String readTillOrB4Dnt){
        if(startFromBeginning)parser=0;
        String nextLn = null;
        for(;parser<lines.size();parser++) {
            line = lines.get(parser);
            if((parser+1)<lines.size())nextLn = lines.get(parser+1);
            ohlc.update(line);
            updateIndicators();
            if(!readB4 && line.startsWith(readTillOrB4Dnt)) break;
            else if(readB4 && nextLn!=null && nextLn.startsWith(readTillOrB4Dnt))break;
        }
    }

    public void loadIndicators(boolean loadVwap,
                               List<Object> bbInputs){

        if(series==null)series = loadSeries();
        if(loadVwap)vwapEntity = new VWAPEntity();
        if(bbInputs!=null) {
            int bbPeriod = Integer.parseInt(bbInputs.get(0).toString()),
                    bbSd = Integer.parseInt(bbInputs.get(1).toString());
            String key  = bbPeriod+":"+bbSd;

            bollingerBandMap.put(key,new BollingerBand(series,bbPeriod,bbSd));
//            System.out.println(bollingerBandMap.size());
        }
    }

    public void updateIndicators(){
        /*
        if(ohlc.lastLn.equalsIgnoreCase(ohlc.ln))return;
        if(superTrendEntity!=null) superTrendEntity.update(ohlc, atrIndicatorForSuperTrend.getValue(parser).doubleValue());
        if(vwapEntity!=null)vwapEntity.setVWAP(ohlc);
        */
    }

    public Optional<String> getBollingerBands(int bbPeriod, int bbSd){
        String key  = bbPeriod+":"+bbSd;
        if(bollingerBandMap.containsKey(key)) {
            BollingerBand st = bollingerBandMap.get(key);
            return Optional.of(st.getBbValuesInString(parser));
        }

        return Optional.empty();
    }

    public Optional<Map<BollingerBand.BBType,Double>> getBollingerBandValues(int bbPeriod, int bbSd){
        String key  = bbPeriod+":"+bbSd;
        if(bollingerBandMap.containsKey(key)) {
            BollingerBand st = bollingerBandMap.get(key);
            return Optional.of(st.getBbValues(false,parser));
        }

        return Optional.empty();
    }
    
    
    public Optional<Map<BollingerBand.BBType,Double>> getBollingerBandValuesAtLb(int lbPeriod,int bbPeriod, int bbSd){
        String key  = bbPeriod+":"+bbSd;
        int lbParser = parser - lbPeriod;
        if(bollingerBandMap.containsKey(key) && lbParser>=0) {
            BollingerBand st = bollingerBandMap.get(key);
            return Optional.of(st.getBbValues(true,lbParser));
        }

        return Optional.empty();
    }

    
    public String getNext(boolean updateIndicators){
        parser++;
        finished = parser >= lines.size();
        if(!finished){
            line = lines.get(parser);
            ohlc.update(line);
            if(ohlc.time.equalsIgnoreCase("09:15")) gapPercent = Math.abs(ohlc.open - ohlc.lastMinClose)/ohlc.lastMinClose * 100;
            if(updateIndicators)
                updateIndicators();
            return line;
        }else return null;
    }

    public float getGapPercent(){
        return gapPercent;
    }

    public void updateLineIndex(int parser){
        this.parser = parser;
        line = lines.get(parser);
        ohlc.update(line);
    }
}
