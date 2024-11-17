package com.valar.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static com.valar.orderPlacer.OrderPlacer.getRoundValue;

public class LimitRange {

    List<Range> ranges = new ArrayList<>(),
            futureRanges = new ArrayList<>();

    class Range {
        private float from = -Float.MAX_VALUE, to = Float.MAX_VALUE;
        private float tightest;
        private boolean isTimes;

        public Range(String s) {
            String[] splits = s.split(",");
            if (!splits[0].equalsIgnoreCase("-"))
                from = Float.parseFloat(splits[0]);
            if (!splits[1].equalsIgnoreCase("-"))
                to = Float.parseFloat(splits[1]);
            tightest = Float.parseFloat(splits[2]);
            isTimes = splits[3].equalsIgnoreCase("y");
        }

        @Override
        public String toString() {
            return "from "+from+" to "+to+" tightest "+tightest+" isTimes "+isTimes;
        }
    }

    public LimitRange() {
        this.update();
    }

    public void update() {
        List<Range> savedRanges = new ArrayList<>(ranges),
                savedFutureRanges = new ArrayList<>(futureRanges);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(".\\Execution Range Strategies.csv"));
            reader.readLine();
            String s;
            ranges.clear();
            futureRanges.clear();
            List<Range> updateRange = ranges;
            while ((s = reader.readLine()) != null) {
                if (s.isEmpty() || s.contains(",,")) {
                    updateRange = futureRanges;
                    continue;
                }
                updateRange.add(new Range(s));
            }
            reader.close();
        } catch (Exception e) {
            ranges = new ArrayList<>(savedRanges);
            futureRanges = new ArrayList<>(savedFutureRanges);
            e.printStackTrace();
        }
    }

    public double getLimitPrice(boolean isFuture,boolean isSell, double ltp) {
        List<Range> executionRange;
        if(isFuture)executionRange = futureRanges;
        else executionRange = ranges;

        double limitPrice = 0;
        for(Range range : executionRange){
            if(ltp >= range.from && ltp < range.to){
                if(isSell){
                    if(range.isTimes)limitPrice = Math.max(0.05,ltp - (range.tightest * ltp));
                    else limitPrice = Math.max(0.05,ltp - range.tightest);
                }else{
                    if(range.isTimes)limitPrice = ltp + (range.tightest * ltp);
                    else limitPrice = ltp + range.tightest;
                }
                break;
            }
        }

        return getRoundValue(limitPrice);
    }
}
