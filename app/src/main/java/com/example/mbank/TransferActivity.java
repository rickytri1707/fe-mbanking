package com.example.mbank;

import com.example.mbank.Config;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

public class TransferActivity extends AppCompatActivity {

    private Button btnDaftar, btnTrf;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        btnDaftar = findViewById(R.id.btnDaftar);
        btnTrf = findViewById(R.id.btnTrf);

        Bundle b = getIntent().getExtras();
        final String sAccNo = b.getString("accNo");

        final Config cfg = new Config();
        btnDaftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TransferActivity.this);
                builder.setTitle("Daftar Rekening Tujuan Transfer");
                final EditText edtInput = new EditText(TransferActivity.this);
                edtInput.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(edtInput);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String sValue = edtInput.getText().toString().trim();

                        if(sValue.equals(sAccNo)){
                            Toast.makeText(TransferActivity.this, "Invalid Account Number", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Log.d("toAccNo", "Value " + sValue);
                        try {
                            JSONObject jdata = new JSONObject();
                            jdata.put("mti", "0200");
                            jdata.put("pcode", "390000");
                            jdata.put("stan", cfg.GenerateStan());
                            jdata.put("gmt", cfg.GetDateTimeGMT());
                            jdata.put("rrn", cfg.GenerateStan() + cfg.GenerateStan());
                            jdata.put("termId", String.format("%-10s", "Android"));
                            jdata.put("accNo", sAccNo);
                            jdata.put("toAccNo", sValue);
                            JSONObject jsend = new JSONObject();
                            jsend.put("MBANKReq", jdata);

                            StringBuilder sbResponse = new StringBuilder();
                            int nSend = cfg.SendLink(jsend.toString(), sbResponse);
                            if(nSend != 200){
                                Toast.makeText(TransferActivity.this, "Http Code Error " + nSend, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Log.d("I", sbResponse.toString());
                            JSONObject jResp = new JSONObject(sbResponse.toString());
                            JSONObject jTagResp = jResp.getJSONObject("MBANKRsp");
                            String sRcode = jTagResp.get("rCode").toString();
                            if(sRcode.equals("00")){
                                JSONObject jDetail = jTagResp.getJSONObject("detailData");
                                String sToAccName = jDetail.get("toAccName").toString();

                                String sMsg = "No Rekening Tujuan: " + sValue + "\r\n" + "Nama: " + sToAccName + "\r\n" + "Berhasil ditambahkan";
                                SharedPreferences pref = getApplicationContext().getSharedPreferences(sAccNo, 0);
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putString(sValue, sValue + "-" + sToAccName);
                                editor.commit();
                                Toast.makeText(TransferActivity.this, sMsg, Toast.LENGTH_SHORT).show();
                            }else{
                                String sDescr = jTagResp.get("descr").toString();
                                Toast.makeText(TransferActivity.this, "RC-" + sRcode + " - " + sDescr, Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                            Log.d("D", e.toString());
                            Toast.makeText(TransferActivity.this, "Exception Error Occured", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        btnTrf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TransferActivity.this, TransferActionActivity.class);
                i.putExtra("accNo", sAccNo);
                startActivity(i);
            }
        });
    }
}
