package com.iirs.soilquality;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.iirs.soilquality.R;

import org.json.JSONException;
import org.json.JSONObject;
import java.text.DecimalFormat;

public class OutputActivity extends AppCompatActivity {
    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.output);

        EditText org_c_val = findViewById(R.id.org_c_value);
        EditText nit_val = findViewById(R.id.nit_value);
        EditText clay_val = findViewById(R.id.clay_value);
        DecimalFormat decimalFormat = new DecimalFormat("#.###");

        // Retrieving JSON string from soil texture activity
        String jsonResponse = getIntent().getStringExtra("responseData");
        try {
            // Parse the JSON string into a JSON object
            JSONObject json = new JSONObject(jsonResponse);
            // Extract the required values from the JSON object using their keys
            double organic_c_value = json.getDouble("message_org_c");
            if(organic_c_value <0) organic_c_value = 0.001;
            double nitrogen_value = json.getDouble("message_nit");
            double clay_value = json.getDouble("message_clay");

            String oc = decimalFormat.format(organic_c_value);
            org_c_val.setText(oc);
            String nit = decimalFormat.format(nitrogen_value);
            nit_val.setText(nit);
            String cla = decimalFormat.format(clay_value);
            clay_val.setText(cla);

            TextView textView = findViewById(R.id.soilhealth);
            if(organic_c_value<0.5){
                textView.setText("LOW");
                textView.setTextColor(Color.BLACK);
                textView.setBackgroundColor(Color.rgb(210, 43, 43));
            } else if (organic_c_value>0.75) {
                textView.setText("HIGH");
                textView.setTextColor(Color.BLACK);
                textView.setBackgroundColor(Color.GREEN);
            }
            else {
                textView.setText("MEDIUM");
                textView.setTextColor(Color.BLACK);
                textView.setBackgroundColor(Color.YELLOW);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}

