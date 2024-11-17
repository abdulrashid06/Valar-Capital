package com.valar.states;

import com.valar.accountAttributes.AccountAttributes;
import com.valar.application.ValarTrade;
import com.valar.entities.Ohlc;
import com.valar.utils.PrintAttribs;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.valar.application.ValarTrade.allAccountAttributes;
import static com.valar.application.ValarTrade.printInfo;

public class StockState extends State{

    public StockState(boolean isIndex,long token, long symphonyToken, String peOrCe, String symbol){
        super(isIndex,token,symphonyToken,peOrCe,symbol);
    }
}
