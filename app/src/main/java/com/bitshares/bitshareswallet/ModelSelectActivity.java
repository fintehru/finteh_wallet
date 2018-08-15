package com.bitshares.bitshareswallet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class ModelSelectActivity extends AppCompatActivity {
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_select);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(v -> finish());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.textViewAccountModel).setOnClickListener(v -> {
            Intent intent = new Intent(ModelSelectActivity.this, ImportActivty.class);
            intent.putExtra("model", ImportActivty.ACCOUNT_MODEL);
            startActivity(intent);
        });

        findViewById(R.id.textViewWalletModelWifKey).setOnClickListener(v -> {
            Intent intent = new Intent(ModelSelectActivity.this, ImportActivty.class);
            intent.putExtra("model", ImportActivty.WALLET_MODEL_WIF_KEY);
            startActivity(intent);
        });

        findViewById(R.id.textViewWalletModelBin).setOnClickListener(v -> {
            Intent intent = new Intent(ModelSelectActivity.this, ImportActivty.class);
            intent.putExtra("model", ImportActivty.WALLET_MODEL_BIN_FILE);
            startActivity(intent);
        });

        findViewById(R.id.textViewWalletModelBrainKey).setOnClickListener(v -> {
            Intent intent = new Intent(ModelSelectActivity.this, ImportActivty.class);
            intent.putExtra("model", ImportActivty.WALLET_MODEL_BRAIN_KEY);
            startActivity(intent);
        });
    }
}
