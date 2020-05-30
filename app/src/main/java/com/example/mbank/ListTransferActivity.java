package com.example.mbank;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListTransferActivity extends AppCompatActivity {
    private ListView listTransfer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_transfer);

        listTransfer = findViewById(R.id.listTransfer);
        Bundle b = getIntent().getExtras();
        final String sFromAcc = b.getString("accNo");

        SharedPreferences settings = getSharedPreferences(sFromAcc, 0);
        Map<String, ?> keys = settings.getAll();
        List<String> values = new ArrayList<String>();
        for(Map.Entry<String, ?> entry : keys.entrySet()){
            values.add(entry.getValue().toString());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
        listTransfer.setAdapter(adapter);

        listTransfer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getAdapter().getItem(position).toString();

                String sToAccNo = selected.substring(0,10);
                Intent i = new Intent(ListTransferActivity.this, TransferActionActivity.class);
                i.putExtra("toAccNo", sToAccNo);
                i.putExtra("accNo", sFromAcc);
                startActivity(i);

            }
        });

    }
}
