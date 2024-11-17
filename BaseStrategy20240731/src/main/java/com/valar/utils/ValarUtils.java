package com.valar.utils;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.DecimalNum;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ValarUtils {
    private static final DateTimeFormatter MIN_FILE_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm"),
            DAY_FILE_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yy");

    public static String date = new SimpleDateFormat("dd-MM-yy").format(new Date());

    public static BarSeries loadSeries(List<String> lines,boolean belongsToMin){
        DateTimeFormatter DATE_TIME_FORMATTER;
        if(belongsToMin)DATE_TIME_FORMATTER = MIN_FILE_DATE_TIME_FORMATTER;
        else DATE_TIME_FORMATTER = DAY_FILE_DATE_TIME_FORMATTER;

        BarSeries series = new BaseBarSeries();
        String ln,dnt;
        for(int i=0;i<lines.size();i++) {
            ln = lines.get(i);
            String[] lineSplits = ln.split(",");
            dnt = lineSplits[0];

            ZonedDateTime date;
            if(belongsToMin) {
                date = LocalDateTime.parse(dnt, DATE_TIME_FORMATTER)
                        .atZone(ZoneId.systemDefault());
            }else {
                LocalDate localDate = LocalDate.parse(dnt, DATE_TIME_FORMATTER);
                date = localDate.atStartOfDay(ZoneId.systemDefault());
            }

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

    public static List<String> getUpdateLinesAccToPeriod(List<String> lines,int period){
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

        return updatedLines;
    }
}
