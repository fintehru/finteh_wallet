package com.bitshares.bitshareswallet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SignUpButtonActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_up_button);
        findViewById(R.id.sign_up_button).setOnClickListener(v -> {
            Intent intent = new Intent(SignUpButtonActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.buttonLogin).setOnClickListener(v -> {
            Intent intent = new Intent(SignUpButtonActivity.this, ModelSelectActivity.class);
            startActivity(intent);
        });
    }
}
