package com.valar.processors;

import com.valar.application.ValarTrade;
import com.valar.entities.BollingerBand;
import com.valar.entities.Ohlc;
import com.valar.indices.Index;
import com.valar.states.StockState;
import com.valar.entities.TickState;
import com.valar.subscription.TickStateSubscription;
import com.valar.utils.ValarUtils;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.num.DecimalNum;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;

public class IndicatorsProcessor implements Flow.Processor<TickState, TickState> {
    private List<TickStateSubscription> subscribers = new ArrayList<>();
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    DateTimeFormatter MIN_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm");
    private Map<Integer,BarSeries> seriesMap ;
    private  Map<String, BollingerBand> bollingerBandMap;
    private Map<Integer,Boolean> indicatorUpdatedForPeriodMap;

    public IndicatorsProcessor(Map<Integer,BarSeries> seriesMap,Map<Integer,Boolean> indicatorUpdatedForPeriodMap){
        this.seriesMap = seriesMap;
        this.indicatorUpdatedForPeriodMap = indicatorUpdatedForPeriodMap;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(TickState tickState) {
        try {
            if(!seriesMap.containsKey(tickState.belongsToCandlePeriod))return;

            Ohlc ohlc = tickState.getOhlc();
            LocalTime lt = LocalTime.now();
            ZonedDateTime date = LocalDateTime.parse(ValarUtils.date+" "+ lt.format(timeFormatter), MIN_DATE_TIME_FORMATTER)
                    .atZone(ZoneId.systemDefault());
            BaseBar bar = BaseBar.builder(DecimalNum::valueOf, Number.class)
                    .timePeriod(Duration.ofMinutes(1))
                    .endTime(date)
                    .openPrice(ohlc.getOpen())
                    .highPrice(ohlc.getHigh())
                    .lowPrice(ohlc.getLow())
                    .closePrice(ohlc.getClose())
                    .volume(ohlc.getVolume())
                    .build();

            seriesMap.get(tickState.belongsToCandlePeriod).addBar(bar);

            indicatorUpdatedForPeriodMap.put(tickState.belongsToCandlePeriod,true);
            subscribers.forEach(subscriber -> subscriber.onNext(tickState));
        }catch (Exception e){
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ValarTrade.printError(exceptionAsString+","+e.getCause()+","+e.toString()+","+e.getMessage()+","+e.getStackTrace()+","+e.getLocalizedMessage());
        }
    }


    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onComplete() {}


    @Override public void subscribe(Flow.Subscriber<? super TickState> subscriber) {
        TickStateSubscription subscription = new TickStateSubscription(subscriber);
        subscriber.onSubscribe(subscription);
        subscribers.add(subscription);
    }

}
