package capital.valar.supertrend.state.day;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.num.DecimalNum;

import capital.valar.supertrend.entities.Ohlc;
import capital.valar.supertrend.entities.SuperTrendEntity;
import capital.valar.supertrend.entities.VWAPEntity;

public class DayState {
    public List<String> lines;
    public int parser;
    public String name,line;
    public Ohlc ohlc = new Ohlc();
    public boolean finished;
    private String dateTimeFormat;
    private float gapPercent;
    private BarSeries series;
    ATRIndicator atrIndicatorForSuperTrend;
    public VWAPEntity vwapEntity;
    public SuperTrendEntity superTrendEntity;
    public Map<String,Double> atrMap = new HashMap<>();

    public DayState(String name, String path, int parser, String dateTimeFormat){
        this.name = name;
        this.parser = parser;
        try{ this.lines = Files.readAllLines(Paths.get(path)); }catch (Exception e){e.printStackTrace();}
        line = lines.get(parser);
        ohlc.update(line);
        this.dateTimeFormat = dateTimeFormat;
        finished = parser >= lines.size();
    }

    public DayState(String name, String path, String readTill, String dateTimeFormat){
        this.name = name;
        try{ this.lines = Files.readAllLines(Paths.get(path)); }catch (Exception e){e.printStackTrace();}
        for(parser =0;parser<lines.size();parser++) {
            line = lines.get(parser);
            if(line.startsWith(readTill)) break;
        }
        ohlc.update(line);
        this.dateTimeFormat = dateTimeFormat;
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
            ZonedDateTime date = LocalDateTime.parse(dnt+" 12:00", DATE_TIME_FORMATTER).atZone(ZoneId.systemDefault());
            double openPrice = Double.parseDouble(lineSplits[1]);
            double highPrice = Double.parseDouble(lineSplits[2]);
            double lowPrice = Double.parseDouble(lineSplits[3]);
            double closePrice = Double.parseDouble(lineSplits[4]);
            double volume = 0;
            if(openPrice==0 || highPrice==0 || closePrice==0 || lowPrice==0) {
            	continue;
            }else {
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

    public void readTillDate(boolean startFromBeginning,String readTill){
        if(startFromBeginning)parser=0;
        for(;parser<lines.size();parser++) {
            line = lines.get(parser);
            ohlc.update(line);
            if(line.startsWith(readTill))break;
        }
    }

    public void readTillDateWhileUpdatingIndicators(boolean startFromBeginning,String readTill){
        if(startFromBeginning)parser=0;
        for(;parser<lines.size();parser++) {
            line = lines.get(parser);
            ohlc.update(line);
            updateIndicators();
            if(line.startsWith(readTill))break;
        }
    }

    public void loadIndicators(boolean loadVwap,
                               List<Object> superTrendATRPeriodAndMultiplier){

        if(series==null)series = loadSeries();
        if(loadVwap)vwapEntity = new VWAPEntity();
        if(superTrendATRPeriodAndMultiplier!=null) {
            atrIndicatorForSuperTrend = new ATRIndicator(series, Integer.parseInt(superTrendATRPeriodAndMultiplier.get(0).toString()));
            superTrendEntity = new SuperTrendEntity(Double.parseDouble(superTrendATRPeriodAndMultiplier.get(1).toString()));
        }
    }

    public void loadATR(int atrPeriod)throws Exception{
        System.out.println();
        if(series==null)series = loadSeries();
        ATRIndicator atrIndicator = new ATRIndicator(series,atrPeriod);
        double lastDayATRPercent = 0;
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                ,outputFormat = new SimpleDateFormat("dd-MM-yy");
        for(int i=atrPeriod;i<atrIndicator.getBarSeries().getEndIndex();i++) {
        	Bar bar = series.getBar(i);
            atrMap.put(outputFormat.format(inputFormat.parse(bar.getSimpleDateName())),lastDayATRPercent);
            lastDayATRPercent = (atrIndicator.getValue(i).doubleValue() / bar.getClosePrice().doubleValue()) * 100;
//            System.out.println(bar.getDateName() + "  "+ lastDayATRPercent);
        }
    }
    
    public double getLastDayAtr(String date) {
    	double lastDayAtr = 0;
    	if(atrMap.containsKey(date)) lastDayAtr = atrMap.get(date);
    	return lastDayAtr;
    }

    public void loadSuperTrend(int atrPeriod,double superTrendMultiplier){
        if(series==null)series = loadSeries();
        atrIndicatorForSuperTrend = new ATRIndicator(series, atrPeriod);
        superTrendEntity = new SuperTrendEntity(superTrendMultiplier);
    }

    public void updateIndicators(){
        if(ohlc.lastLn.equalsIgnoreCase(ohlc.ln))return;
        if(superTrendEntity!=null) superTrendEntity.update(ohlc, atrIndicatorForSuperTrend.getValue(parser).doubleValue());
        if(vwapEntity!=null)vwapEntity.setVWAP(ohlc);
    }

    public String getIndicatorsValues(){
        String vwapValue = null,superTrendAtrValue = null,atrValue = null
        ,superTrendValue = null,parabolicSarValue = null,rsiValue = null;
        if(atrIndicatorForSuperTrend !=null)superTrendAtrValue = "atr : "+ atrIndicatorForSuperTrend.getValue(parser)+"";
        if(superTrendEntity!=null)superTrendValue = "{ sign : "+ superTrendEntity.superTrendSign+", value : "+superTrendEntity.st+" }";
        if(vwapEntity!=null)vwapValue = vwapEntity.getVwap()+"";
        return "{ "+ohlc.dnt+" vwap : "+vwapValue+" , atr : "
                +atrValue+" , superTrendAtr : "+superTrendAtrValue+" , supertrend : "
                +superTrendValue+" , parabolicSAR : "+parabolicSarValue+" , rsi : "+rsiValue+" }";
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