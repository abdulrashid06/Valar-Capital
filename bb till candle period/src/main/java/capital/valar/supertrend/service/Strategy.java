package capital.valar.supertrend.service;
//13-05-20 10:30 close 19767.05 st 19706.05 sar 19681.13 rsi 64-66
import capital.valar.supertrend.entities.BollingerBand;
import capital.valar.supertrend.state.day.DayState;
import capital.valar.supertrend.tradeAndDayMetrics.DayMetric;
import capital.valar.supertrend.tradeAndDayMetrics.OverAllMetric;
import capital.valar.supertrend.entities.Ohlc;
import capital.valar.supertrend.state.day.IndexDayState;
import capital.valar.supertrend.state.minute.IndexState;
import capital.valar.supertrend.state.minute.OptionState;
import capital.valar.supertrend.state.minute.State;
import capital.valar.supertrend.utils.KeyValues;
import capital.valar.supertrend.utils.Options;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static capital.valar.supertrend.utils.Global.*;

public class Strategy {
    private Map<String,Integer> daysMap = new HashMap();
    private String dateTimeFormat = "dd-MM-yy HH:mm";
    private DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy");
    private IndexState indexState;
	private IndexDayState indexDayState;
	private boolean positional;
	private int candlePeriod;

    class OverAllMetricInfo{
        public OverAllMetric overAllMetric = new OverAllMetric();
        public Map<String,DayMetric> dayMetricsMap = new HashMap();
        String keystoreLn;
        float costPercent;
        public OverAllMetricInfo(String keystoreLn,float costPercent){
            this.keystoreLn = keystoreLn;
            this.costPercent = costPercent;
        }
    }

    public Map<Integer,OverAllMetricInfo> overAllMetricInfoMap = new HashMap<>();
    private List<KeyValues> runForKeyAttribs;
    
    public Strategy(boolean positional,int candlePeriod,List<KeyValues> runForKeyAttribs){
    	this.positional=positional;
    	this.candlePeriod=candlePeriod;
        this.runForKeyAttribs = runForKeyAttribs;
        for(KeyValues kv : runForKeyAttribs){
            overAllMetricInfoMap.put(kv.sno,new OverAllMetricInfo(kv.ln,kv.costPercent));
        }

    }

   public void getTradingDatesAndReadTillStartTime()throws Exception{
       while(!indexState.finished){
           applyStrategy();
       }
    }

    public void applyStrategy(){
        Ohlc indexOhlc = indexState.ohlc,nextOhlc = indexState.nextOhlc;

        List<StrategyImpl> strategyImpls = new ArrayList<>();
        for(KeyValues kv : runForKeyAttribs){
            strategyImpls.add(new StrategyImpl(kv,indexDayState,indexState,overAllMetricInfoMap.get(kv.sno).dayMetricsMap));
        }

        boolean dayConditionSatisfied = true;
        if(!dayConditionSatisfied)return;

        do {
            indexState.getNext(true);

            int mins = indexOhlc.mins;
            strategyImpls.parallelStream().forEach(strategyImpl -> {
                if(!strategyImpl.dayExited)
                    strategyImpl.iterate(mins,indexOhlc.dnt);
            });

            if(!indexOhlc.date.equals(nextOhlc.date) || indexState.finished) {
            	overAllMetricInfoMap.values().forEach(metricInfo-> {
                    if (metricInfo.dayMetricsMap.containsKey(indexOhlc.date)) {
                        DayMetric dayMetric = metricInfo.dayMetricsMap.get(indexOhlc.date);
                        dayMetric.updateCostRelatedMetrics();
                    }
                });
            	
            	if (!positional) {
                    break;
                }
            }

        }while(!indexState.finished);

//        overAllMetricInfoMap.values().forEach(metricInfo-> {
//            if (metricInfo.dayMetricsMap.containsKey(indexOhlc.date)) {
//                DayMetric dayMetric = metricInfo.dayMetricsMap.get(indexOhlc.date);
//                dayMetric.updateCostRelatedMetrics();
//            }
//        });
    }

    private boolean readTillDate(String optionPrefix,String readTillDnt,List<OptionState> allOptionsStatePE,
                              List<OptionState> allOptionsStateCE,Map<String,OptionState> allOptionStates){
        ArrayList[] filesRelatedToExpiry = Options.getFiles(optionPrefix);
        ArrayList<String> filesPE = filesRelatedToExpiry[0],filesCE = filesRelatedToExpiry[1];
        int parser = -1;
        for (int i = 0; i < filesPE.size(); i++) {
            String filePE = filesPE.get(i), fileCE = filesCE.get(i);
            OptionState ope;
            if(parser == -1){
                ope = new OptionState(optionPrefix,filePE,getInitPath(filePE) + filePE,true,readTillDnt,dateTimeFormat,false);
                parser = ope.parser;
                if(ope.finished)return false;
            } else ope = new OptionState(optionPrefix,filePE,getInitPath(filePE) + filePE,parser,dateTimeFormat,false);
            allOptionsStatePE.add(ope);
            allOptionStates.put(ope.name, ope);

            OptionState oce = new OptionState(optionPrefix,fileCE,getInitPath(fileCE) + fileCE,parser,dateTimeFormat,false);
            allOptionsStateCE.add(oce);
            allOptionStates.put(oce.name, oce);
        }

        return true;
    }

    private void continueFurtherReadingForSameExpiryFiles(String date,Map<String,OptionState> allOptionStates){
        for(OptionState os : allOptionStates.values())
            os.readTillDate(false,true,date+" 09:15");
    }

    public void apply()throws Exception{
        indexState = new IndexState(indexFile,indexFile,0,dateTimeFormat,candlePeriod,true);
        indexDayState = new IndexDayState("BankNiftyDay",indexDayFile,0,dateTimeFormat);
        for(KeyValues kv : runForKeyAttribs) {
        	indexDayState.loadATR(14);
            indexState.loadIndicators(false, Arrays.asList(kv.period, kv.sd));
        }

        getTradingDatesAndReadTillStartTime();
    }

    private List<String> getKeyValuesAsString(String file,int sno){
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            br.readLine();
            String s;
            List<String> ifNoneFound = new ArrayList<>();
            boolean found = false;
            List<String> list = new ArrayList();
            while ((s = br.readLine()) != null) {
                int hsno = Integer.parseInt(s.split(",")[0]);
                if (!found && hsno == sno) found = true;
                if (hsno == sno || hsno == 2000)
                    list.add(s);
                else if (hsno == 1000)
                    ifNoneFound.add(s);
            }

            if (list.size() == 0 || !found)
                for (String ss : ifNoneFound) list.add(ss);

            if (list.size() == 0) {
                System.err.println("No KeyStore Value found in "+file+" for keystore "+sno);
                System.exit(0);
            }
//            System.out.println("For "+file+" ks is "+list);

            return list;
        }catch (Exception e){ e.printStackTrace();System.exit(0); }

        return null;
    }

    public void calculateOverAll(){
        overAllMetricInfoMap.values().forEach(metricInfo->{
            Map<String,DayMetric> dayMetricsMap = metricInfo.dayMetricsMap;
            for(DayMetric dayMetric:dayMetricsMap.values())
                dayMetric.calculateOverAllMetricsAndPrint(metricInfo.keystoreLn);
            System.out.println(metricInfo.keystoreLn.split(",")[0]);

            metricInfo.overAllMetric.update(new ArrayList<>(dayMetricsMap.values()));
            metricInfo.overAllMetric.calculateOverAllMetricsAndPrint(metricInfo.keystoreLn,metricInfo.costPercent);
        });


    }
}
