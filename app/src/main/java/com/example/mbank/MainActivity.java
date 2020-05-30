package com.example.mbank;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private EditText edtNorek;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(android.os.Build.VERSION.SDK_INT > 9){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        edtNorek = findViewById(R.id.edtNorek);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sNorek = edtNorek.getText().toString().trim();
                if(sNorek.isEmpty()){
                    Toast.makeText(MainActivity.this, "Nomor Rekening Wajib diisi", Toast.LENGTH_SHORT).show();
                    return;
                }
                StringBuilder sbResponse = new StringBuilder();
                try {
                    Random rnd = new Random();
                    int nStan = rnd.nextInt(999999) + 000001;
                    String sStan = String.format("%06d", nStan);
                    JSONObject jdata = new JSONObject();
                    jdata.put("mti", "0200");
                    jdata.put("accNo", sNorek);
                    jdata.put("pcode", "300000");
                    jdata.put("stan", sStan);
                    jdata.put("gmt", sStan+sStan+"00");
                    jdata.put("rrn", sStan+sStan);
                    jdata.put("termId", "Android");

                    JSONObject jsend = new JSONObject();
                    jsend.put("MBANKReq", jdata);

                    String sURL = "http://192.168.0.6:5555";
                    URL url = new URL(sURL);
                    HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setRequestProperty("Cache-Control", "no-cache");
                    urlConnection.setRequestProperty("Accept-Encoding", "UTF-8");
                    urlConnection.setDoOutput(true);
                    urlConnection.setReadTimeout(60000);

                    DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                    wr.writeBytes(jsend.toString());
                    wr.flush();
                    wr.close();

                    int nResponseCode = urlConnection.getResponseCode();
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null){
                        response.append(inputLine);
                    }
                    in.close();
                    sbResponse.append(response);

                    if(nResponseCode != 200){
                        Toast.makeText(MainActivity.this, "Response Code Error " + nResponseCode, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    JSONObject jresp = new JSONObject(sbResponse.toString());
                    JSONObject jTagRsp = jresp.getJSONObject("MBANKRsp");
                    String sRcode = jTagRsp.get("rCode").toString();
                    if(sRcode.equals("00")){
                        JSONObject jdetail = jTagRsp.getJSONObject("detailData");
                        String sName = jdetail.get("name").toString();
                        String sAvailableSaldo = jdetail.get("availableSaldo").toString();
                        String sJenisRekening = jdetail.get("jenisRekening").toString();

                        Toast.makeText(MainActivity.this, "Login Success, Welcome " + sName, Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(MainActivity.this, HomeActivity.class);
                        i.putExtra("name", sName);
                        i.putExtra("availableSaldo", sAvailableSaldo);
                        i.putExtra("jenisRekening", sJenisRekening);
                        startActivity(i);
                    }else{
                        String sDescr = jTagRsp.get("descr").toString();
                        Toast.makeText(MainActivity.this, "RC-" + sRcode + " - " + sDescr, Toast.LENGTH_SHORT).show();
                        return;
                    }



                }catch(Exception e){
                    Toast.makeText(MainActivity.this, "Exception Error", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        });
    }
}
