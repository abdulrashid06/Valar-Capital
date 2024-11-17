// © 2003-2013 Adobe Systems Inc. All Rights Reserved.
// This software is proprietary; use is subject to license terms.
package com.valar.utils;

import com.valar.accountAttributes.AccountAttributes;
import com.valar.accountAttributes.ZerodhaAccountAttributes;
import com.valar.application.ValarTrade;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.TokenException;
import com.zerodhatech.models.User;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.*;

/**
 * @author Adobe Systems Inc
 */
public class AuthUtils {

  Map<String,String> requestTokensMap = new HashMap();

  public AuthUtils()throws Exception{
    List<String> lines = Files.readAllLines(Paths.get(".\\ZerodhaRequestTokens.csv"));
    for(String line:lines){
      String account = line.split(",")[0];
      if(account.contains("Valar") || account.contains("valar")){
        if(line.split(",")[0].equalsIgnoreCase("Valar_"+ValarTrade.api))
          requestTokensMap.put(line.split("_")[0],line.split(",")[1]);
      }else{
        requestTokensMap.put(line.split(",")[0],line.split(",")[1]);
      }
    }
  }

  public void loadZerodhaToken(ZerodhaAccountAttributes accountAttributes){
    System.out.println(requestTokensMap+" and accountName "+accountAttributes.accountName);
    if(requestTokensMap.containsKey(accountAttributes.accountName)) {
      accountAttributes.REQUEST_TOKEN = requestTokensMap.get(accountAttributes.accountName);
    }
  }

  public KiteConnect getKiteConnect(ZerodhaAccountAttributes accountAttributes) throws KiteException, IOException {
    String API_KEY,USER_ID,REQUEST_TOKEN,API_SECRET;
    API_KEY = accountAttributes.API_KEY;
    USER_ID = accountAttributes.USER_ID;
    REQUEST_TOKEN = accountAttributes.REQUEST_TOKEN;
    API_SECRET = accountAttributes.API_SECRET;

    KiteConnect kiteConnect = new KiteConnect(API_KEY);
    kiteConnect.setUserId(USER_ID);
    kiteConnect.setEnableLogging(false);
    if (setTokens(accountAttributes,kiteConnect)) {
      return kiteConnect;
    } else {
      String url = kiteConnect.getLoginURL();
      System.out.println("Get Request token using url: " + url);
      kiteConnect.setSessionExpiryHook(() -> System.out.println("session expired"));
      User user = null;
      try{
        user = kiteConnect.generateSession(REQUEST_TOKEN, API_SECRET);
      }catch (TokenException e){
        e.printStackTrace();
        System.out.println("Enter RequestToken for "+accountAttributes.accountName.toLowerCase());
        Scanner scanner = new Scanner(System.in);
        String requestToken = scanner.nextLine();
        String filePath = ".\\"+accountAttributes.accountName+"RequestToken"+ValarTrade.api+".txt";
        new File(filePath).createNewFile();
        Files.write(Paths.get(filePath), Arrays.asList(requestToken), StandardOpenOption.TRUNCATE_EXISTING);
        loadZerodhaToken(accountAttributes);
        kiteConnect = getKiteConnect(accountAttributes);
        return kiteConnect;
      }
      kiteConnect.setPublicToken(user.publicToken);
      kiteConnect.setAccessToken(user.accessToken);
      saveTokens(accountAttributes,Arrays.asList(LocalDate.now().toString(),user.accessToken,user.publicToken));
    }


    return kiteConnect;
  }

  private String getAccessTokenPath(ZerodhaAccountAttributes aa){
    if(aa.accountName.equalsIgnoreCase("Valar")) aa.accessTokenPath = ".\\"+aa.accountName.toLowerCase()+"-access-token-"+ValarTrade.api;
    else aa.accessTokenPath = ".\\"+aa.accountName.toLowerCase()+"-access-token";
    try {
      File file = new File(aa.accessTokenPath);
      if (!file.exists()) file.createNewFile();
    }catch (Exception e){}
    return aa.accessTokenPath;
  }


  private boolean setTokens(AccountAttributes aa,KiteConnect kiteConnect) {
    try {

      String accessTokenPath = getAccessTokenPath((ZerodhaAccountAttributes) aa);

      List<String> lines = Files.readAllLines(Paths.get(accessTokenPath));
      if (lines != null && lines.size() > 0) {
        LocalDate localDate = LocalDate.parse(lines.get(0));
        if (localDate.equals(LocalDate.now())) {
          kiteConnect.setAccessToken(lines.get(1));
          kiteConnect.setPublicToken(lines.get(2));
          return true;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  public String getSymphonyToken(String filePath,String url,String secretKey,String appKey)throws Exception{
    String interactiveAccessToken = null;
    File file = new File(filePath);
    if(!file.exists())file.createNewFile();
    List<String> lines = Files.readAllLines(Paths.get(filePath));
    if (lines != null && lines.size() > 0) {
      LocalDate localDate = LocalDate.parse(lines.get(0));
      if (localDate.equals(LocalDate.now()))
        interactiveAccessToken = lines.get(1);
    }

    if(interactiveAccessToken==null){
      interactiveAccessToken = loadInteractiveAccessToken(url,secretKey,appKey);
      if(!interactiveAccessToken.isEmpty() && interactiveAccessToken!=null)
        Files.write(Paths.get(filePath), Arrays.asList(LocalDate.now().toString(),interactiveAccessToken)
                , StandardOpenOption.TRUNCATE_EXISTING);
    }
    return interactiveAccessToken;
  }

  private void saveTokens(AccountAttributes aa,List lines) throws IOException {
    String accessTokenPath = getAccessTokenPath((ZerodhaAccountAttributes) aa);
    Files.write(Paths.get(accessTokenPath), lines, StandardOpenOption.TRUNCATE_EXISTING);
  }


  private String loadInteractiveAccessToken(String url,String secretKey,String appKey){
    String interactiveAccessToken = "";
    try {
      DefaultHttpClient httpClient = new DefaultHttpClient();
      String request = url+"/user/session";
      HttpPost postRequest = new HttpPost(request);
      String body = "{\n" +
              "  \"secretKey\": \""+secretKey+"\",\n" +
              "  \"appKey\": \""+appKey+"\",\n" +
              "  \"source\": \"WebApi\""+
              "}";
      StringEntity input = new StringEntity(body);
      postRequest.setHeader("Content-Type", "application/json");
      postRequest.setEntity(input);
      HttpResponse response = httpClient.execute(postRequest);
      BufferedReader br = new BufferedReader(
              new InputStreamReader((response.getEntity().getContent())));
      String output;
      System.out.println("Output from Server .... \n");
      while ((output = br.readLine()) != null) {
        System.out.println("Response -> "+output);
        JSONObject jsonObject = new JSONObject(output);
        interactiveAccessToken = jsonObject.getJSONObject("result").get("token").toString();
      }
      httpClient.getConnectionManager().shutdown();


    }catch (Exception e) { e.printStackTrace();
      while(true) {
        System.out.println("Are you sure you wanna continue?");
        Scanner scanner = new Scanner(System.in);
        System.out.println("\t\tPress 1 to continue and 2 to exit:");
        int a;
        try{a = scanner.nextInt();}catch (Exception ee){continue;}
        if(a==1)break;else if(a==2)System.exit(0);
      }
    }

    return interactiveAccessToken;
  }


}