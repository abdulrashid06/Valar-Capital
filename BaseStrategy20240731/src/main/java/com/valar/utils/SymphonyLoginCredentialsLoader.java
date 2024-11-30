package com.valar.utils;

import java.util.List;

public class SymphonyLoginCredentialsLoader {
    public String accountName;
    public String broker;
    public String appKey,secretKey;
    public String url;
    public String tokenFile;
    private int iter;
    public int urlIndex,accountIndex;
    public SymphonyLoginCredentialsLoader(List<String> lines, int index){
        this.accountIndex = index;
        accountName = lines.get(iter++).split(",")[index];
        broker = lines.get(iter++).split(",")[index];
        urlIndex = iter;
        url = lines.get(iter++).split(",")[index];
        String appAndSecretKey = lines.get(iter++).split(",")[index];
        appKey = appAndSecretKey.split(":")[0];
        secretKey = appAndSecretKey.split(":")[1];
        tokenFile = lines.get(iter++).split(",")[index];
    }

    public String getAccountName(){
        return accountName;
    }
}
