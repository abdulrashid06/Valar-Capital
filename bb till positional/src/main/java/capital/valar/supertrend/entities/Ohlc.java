package capital.valar.supertrend.entities;

import capital.valar.supertrend.application.ValarTrade;
import capital.valar.supertrend.utils.Global;

import java.util.ArrayList;
import java.util.List;

import static capital.valar.supertrend.utils.Global.getInMinutes;

public class Ohlc {
    public String lastLn="",ln="",dnt,date,lastDayDate,time;
    public int hr,min;

    public int mins;
    public float open,high,low,close,lastDayClose,lastMinClose,volume;
    public int volumePeriod;
    public List<Float> volumes = new ArrayList();
    public Ohlc(String ln){
        update(ln);
    }

    public Ohlc(Ohlc ohlc){
        update(ohlc.ln);
    }

    public Ohlc(){}

    public void update(String ln){
        String[] splits = ln.split(",");
        lastLn = this.ln;
        this.ln = ln;
        dnt = splits[0];
        if(dnt.contains(" ")){
            String[] dntSplits = dnt.split(" ");
            date = dntSplits[0];
            time = dntSplits[1];
            String[] timeSplits = time.split(":");
            hr = Integer.parseInt(timeSplits[0]);
            min = Integer.parseInt(timeSplits[1]);
            mins = getInMinutes(hr,min);
        }else{ date = dnt;time = dnt;}
        open = Float.parseFloat(splits[1]);
        high = Float.parseFloat(splits[2]);
        low = Float.parseFloat(splits[3]);
        lastMinClose = close;
        close = Float.parseFloat(splits[4]);

        if(time.equalsIgnoreCase("15:29") && close!=0) {
            lastDayClose = Float.parseFloat(splits[4]);
            lastDayDate = date;
        }

        if(splits.length>5){
            volume = Float.parseFloat(splits[5]);
            if(volumePeriod>0) {
                volumes.add(volume);
                if (volumes.size() > volumePeriod)
                    volumes.remove(0);
            }
        }
    }

    public void storeVolumesForPeriod(int volumePeriod){
        this.volumePeriod = volumePeriod;
    }

    public boolean isVolumeConditionSatisfied(float minVolume,float minAvgVolume){
        if(volumePeriod==0 || volumes.size()<volumePeriod)return true;
        for(float volume : volumes)
            if(volume < minVolume)
                return false;

        return volumes.stream().mapToDouble(d->d).average().orElse(0) >= minAvgVolume;
    }

    public String toString(){
        return dnt+" open "+open+" ,high "+high+" ,low "+low+" ,close "+close+" ,lastMinClose "+lastMinClose;
    }
}
