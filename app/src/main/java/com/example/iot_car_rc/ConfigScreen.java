package com.example.iot_car_rc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ConfigScreen extends AppCompatActivity {
    private Button confirmIpButton;
    private EditText ipAddressEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_screen);

        this.confirmIpButton = (Button) findViewById(R.id.config_button);
        this.ipAddressEditText = (EditText) findViewById(R.id.ip_address);

        this.confirmIpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConfigScreen.this, MainActivity.class);

                intent.putExtra("IP_ADDRESS", ipAddressEditText.getText().toString());

                startActivity(intent);
            }
        });
    }
}
