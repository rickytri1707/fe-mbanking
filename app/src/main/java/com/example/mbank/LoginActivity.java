package com.example.mbank;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mbank.Config;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Random;



public class LoginActivity extends AppCompatActivity {

    private EditText edtUserId, edtPassword;
    private Button btnLogin;
    private ProgressBar pbarLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        edtUserId = findViewById(R.id.edtUserId);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        pbarLogin = findViewById(R.id.pbarLogin);

        final String sAndroidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Config cfg = new Config();
                String sUserId = edtUserId.getText().toString().trim();
                String sPassword = edtPassword.getText().toString().trim();

                btnLogin.setVisibility(View.GONE);
                pbarLogin.setVisibility(View.VISIBLE);

                if(sUserId.isEmpty() | sPassword.isEmpty()){
                    Toast.makeText(LoginActivity.this, "UserId / Password Tidak Boleh Kosong", Toast.LENGTH_LONG).show();
                    btnLogin.setVisibility(View.VISIBLE);
                    pbarLogin.setVisibility(View.GONE);
                    return;
                }
                sPassword = cfg.md5Hash(sPassword);

                Random rnd = new Random();
                int nStan = rnd.nextInt(999999) + 000001;
                String sStan = String.format("%06d", nStan);
                String sTermId = "Android   " + sAndroidId;
                try {
                    JSONObject jdata = new JSONObject();
                    jdata.put("mti", "0200");
                    jdata.put("pcode", "000000");
                    jdata.put("stan", sStan);
                    jdata.put("gmt", cfg.GetDateTimeGMT());
                    jdata.put("rrn", sStan + sStan);
                    jdata.put("termId", sTermId);
                    jdata.put("userId", sUserId);
                    jdata.put("password", sPassword);
                    JSONObject jsend = new JSONObject();
                    jsend.put("MBANKReq", jdata);

                    StringBuilder sbResponse = new StringBuilder();
                    int nSend = cfg.SendLink(jsend.toString(), sbResponse);
                    if(nSend != 200){
                        Toast.makeText(LoginActivity.this, "HTTP Code Error " + nSend, Toast.LENGTH_SHORT).show();
                    }
                    JSONObject jresp = new JSONObject(sbResponse.toString());
                    JSONObject jTagResp = jresp.getJSONObject("MBANKRsp");
                    String sRcode = jTagResp.get("rCode").toString();
                    if(sRcode.equals("00")){
                        String sAccNo = jTagResp.get("accNo").toString();
                        jdata = new JSONObject();
                        jdata.put("mti", "0200");
                        jdata.put("pcode", "300000");
                        jdata.put("stan", sStan);
                        jdata.put("gmt", cfg.GetDateTimeGMT());
                        jdata.put("rrn", sStan + sStan);
                        jdata.put("termId", "Android");
                        jdata.put("accNo", sAccNo);
                        jsend = new JSONObject();
                        jsend.put("MBANKReq", jdata);
                        sbResponse.setLength(0);
                        nSend = cfg.SendLink(jsend.toString(), sbResponse);
                        if(nSend != 200){
                            Toast.makeText(LoginActivity.this, "Error Login User", Toast.LENGTH_SHORT).show();
                            btnLogin.setVisibility(View.VISIBLE);
                            pbarLogin.setVisibility(View.GONE);
                        }
                        jresp = new JSONObject(sbResponse.toString());
                        jTagResp = jresp.getJSONObject("MBANKRsp");
                        sRcode = jTagResp.get("rCode").toString();
                        if(sRcode.equals("00")){
                            btnLogin.setVisibility(View.VISIBLE);
                            pbarLogin.setVisibility(View.GONE);
                            JSONObject jdetail = jTagResp.getJSONObject("detailData");
                            String sNama = jdetail.get("name").toString();

                            SharedPreferences pref = getApplicationContext().getSharedPreferences(sAccNo+"_SESSION", 0);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("accNo", sAccNo);
                            editor.putString("accName", sNama);
                            editor.putString("userId", sUserId);
                            editor.putString("password", sPassword);
                            editor.putString("androidId", sAndroidId);
                            editor.commit();

                            Toast.makeText(LoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                            i.putExtra("accNo", sAccNo);

                            startActivity(i);
                        }else{
                            btnLogin.setVisibility(View.VISIBLE);
                            pbarLogin.setVisibility(View.GONE);
                            String sDescr = jTagResp.get("descr").toString();
                            Toast.makeText(LoginActivity.this, "RC-" + sRcode + " - " + sDescr, Toast.LENGTH_SHORT).show();
                            return;
                        }

                    }else{
                        btnLogin.setVisibility(View.VISIBLE);
                        pbarLogin.setVisibility(View.GONE);
                        String sDescr = jTagResp.get("descr").toString();
                        Toast.makeText(LoginActivity.this, "RC-" + sRcode + " - " + sDescr, Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    btnLogin.setVisibility(View.VISIBLE);
                    pbarLogin.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Exception Error", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }
}
