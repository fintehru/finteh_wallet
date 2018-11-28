package com.good.code.starts.here.deposit;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.localizationactivity.ui.LocalizationActivity;
import com.bitshares.bitshareswallet.R;
import com.bitshares.bitshareswallet.wallet.BitsharesWalletWraper;
import com.good.code.starts.here.ColorUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DepositActivity extends LocalizationActivity {

    private Gson gson;
    private RecyclerView depositRecyclerView;

    private Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit);

        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        toolbar.setTitle(getResources().getString(R.string.deposit));
        int color = ColorUtils.getMainColor(this);
        toolbar.setBackgroundColor(color);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ColorUtils.manipulateColor(color, 0.75f));
        }

        gson = new Gson();
        OkHttpClient okHttpClient = new OkHttpClient();

        depositRecyclerView = findViewById(R.id.depositRecyclerView);

        depositRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        depositRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        ProgressDialog dialog = ProgressDialog.show(this, getString(R.string.loading_dots), getString(R.string.now_loading_list));

        Request request = new Request.Builder().url("https://gateway.rudex.org/api/v0_3/coins").build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(DepositActivity.this, R.string.an_error, Toast.LENGTH_SHORT).show();
                    finish();
                });

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.body() != null) {
                    DepositCoin[] coins = gson.fromJson(response.body().string(), DepositCoin[].class);
                    runOnUiThread(() -> {
                        depositRecyclerView.setAdapter(new DepositRecyclerAdapter(DepositActivity.this, coins, depositCoin -> {

                            if(depositCoin.getGatewayWallet() == null) {
                                ProgressDialog tokenLoadDialog = ProgressDialog.show(DepositActivity.this, getString(R.string.loading_dots), getString(R.string.now_loading_info));
                                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "{\"inputCoinType\":\"" + depositCoin.getBackingCoin().toLowerCase() + "\",\"outputCoinType\":\"" + depositCoin.getSymbol().toLowerCase() + "\",\"outputAddress\":\"" + BitsharesWalletWraper.getInstance().get_account().name + "\"}");

                                Request request = new Request.Builder()
                                        .url("https://gateway.rudex.org/api/v0_3/wallets/" + depositCoin.getWalletType() + "/new-deposit-address")
                                        .post(requestBody)
                                        .build();

                                okHttpClient.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        runOnUiThread(() -> {
                                            tokenLoadDialog.dismiss();
                                            Toast.makeText(DepositActivity.this, getString(R.string.an_error), Toast.LENGTH_SHORT).show();
                                        });
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) {
                                        DepositResponse depositResponse = null;
                                        try {
                                            depositResponse = gson.fromJson(response.body().string(), DepositResponse.class);
                                        } catch (IOException e) {
                                            tokenLoadDialog.dismiss();
                                            Toast.makeText(DepositActivity.this, R.string.an_error, Toast.LENGTH_SHORT).show();
                                        }
                                        if(depositResponse != null) {
                                            final DepositResponse finalDepositResponse = depositResponse;
                                            runOnUiThread(() -> {
                                                BigDecimal min = new BigDecimal(depositCoin.getMinAmount()).setScale(depositCoin.getPrecision(), RoundingMode.UNNECESSARY).divide(new BigDecimal(Math.pow(10, depositCoin.getPrecision())), RoundingMode.UNNECESSARY);

                                                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

                                                View dialogView = getLayoutInflater().inflate(R.layout.dialog_deposit, null, false);

                                                dialogView.findViewById(R.id.memoContent).setVisibility(View.GONE);
                                                dialogView.findViewById(R.id.memoTitle).setVisibility(View.GONE);
                                                dialogView.findViewById(R.id.copyMemo).setVisibility(View.GONE);

                                                TextView toContent = dialogView.findViewById(R.id.toContent);
                                                TextView minContent = dialogView.findViewById(R.id.minContent);

                                                toContent.setText(finalDepositResponse.getInputAddress());
                                                minContent.setText(min.stripTrailingZeros().toPlainString() + " " + depositCoin.getName());

                                                dialogView.findViewById(R.id.copyTo).setOnClickListener(v -> {
                                                    ClipData clipData = ClipData.newPlainText("deposit to", finalDepositResponse.getInputAddress());
                                                    clipboardManager.setPrimaryClip(clipData);
                                                    Toast.makeText(DepositActivity.this, getString(R.string.copy_success), Toast.LENGTH_SHORT).show();
                                                });

                                                AlertDialog alertDialog = new AlertDialog.Builder(DepositActivity.this)
                                                        .setTitle(getString(R.string.deposite_title) + " " + depositCoin.getName())
                                                        .setView(dialogView)
                                                        .setPositiveButton(R.string.OK, null)
                                                        .create();
                                                tokenLoadDialog.dismiss();
                                                alertDialog.show();
                                            });
                                        }
                                    }
                                });
                            } else {
                                BigDecimal min = new BigDecimal(depositCoin.getMinAmount()).setScale(depositCoin.getPrecision(), RoundingMode.UNNECESSARY).divide(new BigDecimal( Math.pow(10, depositCoin.getPrecision())), RoundingMode.UNNECESSARY);
                                runOnUiThread(() -> {

                                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                    String name = BitsharesWalletWraper.getInstance().get_account().name;

                                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_deposit, null, false);

                                    TextView toContent = dialogView.findViewById(R.id.toContent);
                                    TextView memoContent = dialogView.findViewById(R.id.memoContent);
                                    TextView minContent = dialogView.findViewById(R.id.minContent);

                                    toContent.setText(depositCoin.getGatewayWallet());
                                    memoContent.setText("dex:" + name);
                                    minContent.setText(min.stripTrailingZeros().toPlainString() + " " + depositCoin.getName());

                                    dialogView.findViewById(R.id.copyTo).setOnClickListener(v -> {
                                        ClipData clipData = ClipData.newPlainText("deposit to", depositCoin.getGatewayWallet());
                                        clipboardManager.setPrimaryClip(clipData);
                                        Toast.makeText(DepositActivity.this, getString(R.string.copy_success), Toast.LENGTH_SHORT).show();
                                    });

                                    dialogView.findViewById(R.id.copyMemo).setOnClickListener(v -> {
                                        ClipData clipData = ClipData.newPlainText("deposit memo", "dex:" + name);
                                        clipboardManager.setPrimaryClip(clipData);
                                        Toast.makeText(DepositActivity.this, getString(R.string.copy_success), Toast.LENGTH_SHORT).show();
                                    });

                                    AlertDialog alertDialog = new AlertDialog.Builder(DepositActivity.this)
                                            .setTitle("Deposit " + depositCoin.getName())
                                            .setView(dialogView)
                                            .setPositiveButton(R.string.OK, null)
                                            .create();
                                    alertDialog.show();
                                });
                            }

                        }));
                        dialog.dismiss();
                    });
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
