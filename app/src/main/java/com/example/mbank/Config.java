package com.example.mbank;

import android.os.StrictMode;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;


public class Config {

    static String md5Hash(String sValue){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(sValue.getBytes());
            byte byteData[] = md.digest();
            StringBuffer sb = new StringBuffer();
            for (int i=0; i < byteData.length; i++){
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }

            sValue = sb.toString();
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
            System.exit(1);
        }
        return sValue;
    }
    static String GetDateTimeGMT(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(new Date());
    }

    static String GetDateNow(){
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/mm/YYYY HH:mm:ss");
        return dateFormat.format(date);
    }
    static String GenerateStan(){
        Random rnd = new Random();
        int nStan = rnd.nextInt(999999) + 000001;
        String sStan = String.format("%06d", nStan);
        return sStan;
    }

    public static String sUrl = "http://192.168.0.8:5555";
    static int SendLink(String jdata, StringBuilder sbResponse){
        try {
            if(android.os.Build.VERSION.SDK_INT > 9){
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }
            URL url = new URL(sUrl);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlConn.setRequestProperty("Accept", "application/json");
            urlConn.setRequestProperty("Cache-Control", "no-cache");
            urlConn.setRequestProperty("Accept-Encoding", "UTF-8");
            urlConn.setDoOutput(true);
            urlConn.setReadTimeout(60000);

            DataOutputStream wr = new DataOutputStream(urlConn.getOutputStream());
            wr.writeBytes(jdata);
            wr.flush();
            wr.close();

            int responeCode = urlConn.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null){
                response.append(inputLine);
            }
            in.close();
            sbResponse.append(response);

            return responeCode;

        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }
}
