package com.example.mbank;

import com.example.mbank.Config;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;



public class HomeActivity extends AppCompatActivity {

    private TextView txtNama;
    private Button btnInfoSaldo, btnTransfer, btnMutasi, btnLogout, btnAdmin;
    public String sUserId = "";
    public String sAccNum = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        txtNama = findViewById(R.id.txtNama);
        btnInfoSaldo = findViewById(R.id.btnInfoSaldo);
        btnTransfer = findViewById(R.id.btnTransfer);
        btnMutasi = findViewById(R.id.btnMutasi);
        btnLogout = findViewById(R.id.btnLogout);
        btnAdmin = findViewById(R.id.btnAdmin);


        Bundle b = getIntent().getExtras();

        final String sAccNo = b.getString("accNo");
        sAccNum = sAccNo;

        SharedPreferences settings = getSharedPreferences(sAccNo+"_SESSION", 0);
        String sName = settings.getString("accName", "");
        if(sName.length() > 21) sName = sName.substring(0,21);
        txtNama.setText("Welcome, " + sName);
        final String sUserIds = settings.getString("userId", "");
        sUserId = sUserIds;
        final Config cfg = new Config();
        btnInfoSaldo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random rnd = new Random();
                int nStan = rnd.nextInt(999999) + 000001;
                String sStan = String.format("%06d", nStan);
                try {
                    JSONObject jdata = new JSONObject();
                    jdata.put("mti", "0200");
                    jdata.put("pcode", "300000");
                    jdata.put("stan", sStan);
                    jdata.put("gmt", cfg.GetDateTimeGMT());
                    jdata.put("rrn", sStan + sStan);
                    jdata.put("termId", String.format("%-10s", "Android"));
                    jdata.put("accNo", sAccNo);
                    JSONObject jsend = new JSONObject();
                    jsend.put("MBANKReq", jdata);

                    StringBuilder sbResponse = new StringBuilder();
                    int nSend = cfg.SendLink(jsend.toString(), sbResponse);
                    if(nSend != 200){
                        Toast.makeText(HomeActivity.this, "Info Saldo Gagal", Toast.LENGTH_SHORT).show();
                    }

                    JSONObject jResp = new JSONObject(sbResponse.toString());
                    JSONObject jTagResp = jResp.getJSONObject("MBANKRsp");
                    String sRcode = jTagResp.get("rCode").toString();

                    if(sRcode.equals("00")) {
                        JSONObject jDetail = jTagResp.getJSONObject("detailData");
                        String sAvailableSaldo = jDetail.get("availableSaldo").toString();
                        Double nAvailableSaldo = Double.parseDouble(sAvailableSaldo);
                        DecimalFormat kursIndonesia = (DecimalFormat)DecimalFormat.getCurrencyInstance();
                        DecimalFormatSymbols formatRp = new DecimalFormatSymbols();
                        formatRp.setCurrencySymbol("Rp. ");
                        formatRp.setMonetaryDecimalSeparator(',');
                        formatRp.setGroupingSeparator('.');

                        kursIndonesia.setDecimalFormatSymbols(formatRp);
                        sAvailableSaldo = kursIndonesia.format(nAvailableSaldo);

                        String sMessage = "Saldo Tersedia: " + sAvailableSaldo;
                        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                        builder.setCancelable(false);
                        builder.setTitle("M-Info");
                        builder.setMessage(sMessage);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }else{
                        String sDescr = jTagResp.get("descr").toString();
                        Toast.makeText(HomeActivity.this, "RC-" + sRcode + " - " + sDescr, Toast.LENGTH_SHORT).show();
                    }

                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        });

        btnMutasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random rnd = new Random();
                int nStan = rnd.nextInt(999999) + 000001;
                String sStan = String.format("%06d", nStan);
                try{
                    JSONObject jdata = new JSONObject();
                    jdata.put("mti", "0200");
                    jdata.put("pcode", "330000");
                    jdata.put("stan", sStan);
                    jdata.put("gmt", cfg.GetDateTimeGMT());
                    jdata.put("rrn", sStan + sStan);
                    jdata.put("termId", String.format("%-10s", "Android"));
                    jdata.put("accNo", sAccNo);
                    JSONObject jsend = new JSONObject();
                    jsend.put("MBANKReq", jdata);

                    StringBuilder sbResponse = new StringBuilder();
                    int nSend = cfg.SendLink(jsend.toString(), sbResponse);
                    if(nSend != 200){
                        Toast.makeText(HomeActivity.this, "Http Error Inquiry Mutasu Rekening " + nSend, Toast.LENGTH_LONG).show();
                        return;
                    }

                    JSONObject jResp = new JSONObject(sbResponse.toString());
                    JSONObject jTagResp = jResp.getJSONObject("MBANKRsp");
                    String sRcode = jTagResp.get("rCode").toString();
                    if(sRcode.equals("00")){
                        JSONArray jDetail = jTagResp.getJSONArray("detailData");
                        int nLenDetail = jDetail.length();

                        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
                        StringBuilder sb = new StringBuilder();
                        for (int i=0; i < nLenDetail; i++){
                            JSONObject jTx = jDetail.getJSONObject(i);
                            String jKodeTrx = jTx.get("kodeTrx").toString();
                            String jJenisTrx = "";
                            String jDate = jTx.get("date").toString();
                            String jTime = jTx.get("time").toString();
                            String jAmount = jTx.get("amount").toString();
                            double nAmount = Double.parseDouble(jAmount);

                            DecimalFormat kursIndonesia = (DecimalFormat)DecimalFormat.getCurrencyInstance();
                            DecimalFormatSymbols formatRp = new DecimalFormatSymbols();
                            formatRp.setCurrencySymbol("Rp. ");
                            formatRp.setMonetaryDecimalSeparator(',');
                            formatRp.setGroupingSeparator('.');

                            kursIndonesia.setDecimalFormatSymbols(formatRp);
                            jAmount = kursIndonesia.format(nAmount);

                            String jStan = jTx.get("stan").toString();
                            if(jKodeTrx.equals("39")) jJenisTrx = "Inq Trf";
                            if(jKodeTrx.equals("40")) jJenisTrx = "Post Trf";
                            jDate = jDate.substring(0,2) + "/" + jDate.substring(2,4) + "/" + jDate.substring(4, jDate.length());
                            jTime = jTime.substring(0,2) + ":" + jTime.substring(2,4);
                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put("jenisTrx", jJenisTrx);
                            map.put("date", jDate);
//                            map.put("time", jTime);
                            map.put("amount", jAmount);
//                            map.put("stan", jStan);
                            list.add(map);
                        }

                        for (HashMap map : list){
                            Iterator it = map.entrySet().iterator();
                            while (it.hasNext()){
                                sb.append(((Map.Entry) it.next()).getValue()).append(" ");
                            }
                            sb.append("\n");
                        }
                        String sMsg = sb.toString();

                        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                        builder.setCancelable(false);
                        builder.setTitle("Mutasi Transaksi");
                        builder.setMessage(sMsg);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                    builder.setCancelable(false);
                    builder.setTitle("Exception error");
                    builder.setMessage(e.toString());
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });

        btnTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeActivity.this, TransferActivity.class);
                i.putExtra("accNo", sAccNo);
                startActivity(i);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setCancelable(false);
                builder.setMessage("Do You Want Logout?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Config cfg = new Config();
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
                            jdata.put("userId", sUserIds);
                            JSONObject jsend = new JSONObject();
                            jsend.put("MBANKReq", jdata);

                            StringBuilder sbResponse = new StringBuilder();
                            int nSend = cfg.SendLink(jsend.toString(), sbResponse);
                            if(nSend != 200){
                                Toast.makeText(HomeActivity.this, "Error Logout User", Toast.LENGTH_SHORT).show();
                            }

                            JSONObject jResp = new JSONObject(sbResponse.toString());
                            JSONObject jTagResp = jResp.getJSONObject("MBANKRsp");
                            String sRcode = jTagResp.get("rCode").toString();
                            if(sRcode.equals("00")){
                                SharedPreferences settings = getSharedPreferences(sAccNo+"_SESSION", 0);
                                settings.edit().clear().commit();
                                finish();
                            }
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        btnAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeActivity.this, AdminActivity.class);
                i.putExtra("accNo", sAccNo);
                startActivity(i);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Bundle b = getIntent().getExtras();

        final String sAccNo = b.getString("accNo");

        SharedPreferences settings = getSharedPreferences(sAccNo+"_SESSION", 0);
        final String sUserId = settings.getString("userId", "");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Do You Want Logout?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Config cfg = new Config();
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
                    if(nSend != 200){
                        Toast.makeText(HomeActivity.this, "Error Logout User", Toast.LENGTH_SHORT).show();
                    }

                    JSONObject jResp = new JSONObject(sbResponse.toString());
                    JSONObject jTagResp = jResp.getJSONObject("MBANKRsp");
                    String sRcode = jTagResp.get("rCode").toString();
                    if(sRcode.equals("00")){
                        SharedPreferences settings = getSharedPreferences(sAccNo+"_SESSION", 0);
                        settings.edit().clear().commit();
                        finish();
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /*@Override
    protected void onStop() {
        Config cfg = new Config();
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
                SharedPreferences settings = getSharedPreferences(sAccNum+"_SESSION", 0);
                settings.edit().clear().commit();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }*/
}
