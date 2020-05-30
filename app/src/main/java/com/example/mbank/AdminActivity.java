package com.example.mbank;
import com.example.mbank.Config;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Random;


public class AdminActivity extends AppCompatActivity {

    private Button btnGantiPin, btnSendPin;
    private EditText edtPinLama, edtPinBaru, edtKonfPinBaru;
    public String sAccNo = "";
    public String sUserId = "";
    public String sAccName = "";
    private String sPassword = "";
    Config cfg = new Config();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle b = getIntent().getExtras();
        sAccNo = b.getString("accNo");
        SharedPreferences settings = getSharedPreferences(sAccNo+"_SESSION", 0);
        sUserId = settings.getString("userId", "");
        sAccName = settings.getString("accName", "");
        sPassword = settings.getString("password", "");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        btnGantiPin = findViewById(R.id.btnGantiPin);

        btnGantiPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickMe(sAccName, sUserId, sPassword);
            }
        });

    }

    public void clickMe(String sAccName, final String sUserId, final String sPassword){
        setContentView(R.layout.activity_change_pin);

        btnSendPin = findViewById(R.id.btnSendPin);
        edtPinLama = findViewById(R.id.edtPinLama);
        edtPinBaru = findViewById(R.id.editPinBaru);
        edtKonfPinBaru = findViewById(R.id.edtKonfPinBaru);

        Random rnd = new Random();
        int nStan = rnd.nextInt(999999) + 000001;
        final String sStan = String.format("%06d", nStan);

        btnSendPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sPinLama = cfg.md5Hash(edtPinLama.getText().toString().trim());
                String sPinBaru = cfg.md5Hash(edtPinBaru.getText().toString().trim());
                String sKonfPinBaru = cfg.md5Hash(edtKonfPinBaru.getText().toString().trim());
                String password = sPassword;
                String sUsrId = sUserId;
                if(!sKonfPinBaru.equals(sPinBaru)){
                    Toast.makeText(AdminActivity.this, "Konfirmasi PIN Baru tidak sesuai", Toast.LENGTH_LONG).show();
                    return;
                }
                if(!sPinLama.equals(password)){
                    Toast.makeText(AdminActivity.this, "PIN Lama Tidak Sesuai", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(sPinBaru.equals(sPinLama)){
                    Toast.makeText(AdminActivity.this, "PIN Baru Tidak Boleh Sama dengan PIN Lama", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    JSONObject jdata = new JSONObject();
                    jdata.put("mti", "0200");
                    jdata.put("pcode", "900000");
                    jdata.put("stan", sStan);
                    jdata.put("gmt", cfg.GetDateTimeGMT());
                    jdata.put("rrn", sStan + sStan);
                    jdata.put("termId", String.format("%-10s", "Android"));
                    jdata.put("userId", sUsrId);
                    jdata.put("newPassword", sPinBaru);

                    JSONObject jsend = new JSONObject();
                    jsend.put("MBANKReq", jdata);

                    StringBuilder sbResponse = new StringBuilder();
                    int nSend = cfg.SendLink(jsend.toString(), sbResponse);
                    if(nSend != 200){
                        Toast.makeText(AdminActivity.this, "Error Request Ganti PIN", Toast.LENGTH_SHORT).show();
                    }

                    JSONObject jResp = new JSONObject(sbResponse.toString());
                    JSONObject jTagResp = jResp.getJSONObject("MBANKRsp");
                    String sRcode = jTagResp.get("rCode").toString();
                    if(sRcode.equals("00")){
                        String sDescr = jTagResp.get("descr").toString();
                        SharedPreferences settings = getSharedPreferences(sAccNo+"_SESSION", 0);
                        SharedPreferences.Editor e = settings.edit();
                        e.putString("password", sPinBaru);
                        e.apply();
                        edtPinLama.setText("");
                        edtPinBaru.setText("");
                        edtKonfPinBaru.setText("");
                        Toast.makeText(AdminActivity.this, sDescr, Toast.LENGTH_SHORT).show();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                    Toast.makeText(AdminActivity.this, "Exception Error Proses Ganti PIN", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }

    @Override
    protected void onStop() {
        Random rnd = new Random();
        int nStan = rnd.nextInt(999999) + 000001;
        String sStan = String.format("%06d", nStan);
        try {
            JSONObject jdata = new JSONObject();
            jdata.put("mti", "0200");
            jdata.put("pcode", "000001");
            jdata.put("stan", sStan);
            jdata.put("gmt", cfg.GetDateTimeGMT());
            jdata.put("rrn", sStan + sStan);
            jdata.put("termId", String.format("%-10s", "Android"));
            jdata.put("userId", sUserId);
            JSONObject jsend = new JSONObject();
            jsend.put("MBANKReq", jdata);

            StringBuilder sbResponse = new StringBuilder();
            int nSend = cfg.SendLink(jsend.toString(), sbResponse);

            JSONObject jResp = new JSONObject(sbResponse.toString());
            JSONObject jTagResp = jResp.getJSONObject("MBANKRsp");
            String sRcode = jTagResp.get("rCode").toString();
            if(sRcode.equals("00")){
                SharedPreferences settings = getSharedPreferences(sAccNo+"_SESSION", 0);
                settings.edit().clear().commit();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }
}
