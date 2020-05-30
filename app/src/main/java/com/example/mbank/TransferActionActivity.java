package com.example.mbank;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TransferActionActivity extends AppCompatActivity {

    private TextView txtFromAcc, txtToAcc;
    private Button btnHome;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_action);

        txtFromAcc = findViewById(R.id.txtFromAcc);
        txtToAcc = findViewById(R.id.txtToAcc);

        Bundle b = getIntent().getExtras();

        final String sAccNo = b.getString("accNo");
        String sToAccNo = "";
        try{
            sToAccNo = b.getString("toAccNo");
            txtToAcc.setText(sToAccNo);
        }catch(Exception e){
            sToAccNo = "";
        }
        txtFromAcc.setText(sAccNo);

        txtToAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TransferActionActivity.this, ListTransferActivity.class);
                i.putExtra("accNo", sAccNo);
                startActivity(i);
            }
        });
    }
}
