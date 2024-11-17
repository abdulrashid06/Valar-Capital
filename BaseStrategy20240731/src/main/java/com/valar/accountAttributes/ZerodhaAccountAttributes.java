package com.valar.accountAttributes;

public class ZerodhaAccountAttributes extends AccountAttributes {
    public String accessTokenPath;

    public ZerodhaAccountAttributes(String lotValFile,String broker,String accountName, int index) {
        super(lotValFile,broker,accountName, index);
    }
}
