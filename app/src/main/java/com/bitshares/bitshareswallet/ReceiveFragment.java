package com.bitshares.bitshareswallet;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.TextInputEditText;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bitshares.bitshareswallet.wallet.BitsharesWalletWraper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class ReceiveFragment extends BaseFragment {

    private ImageView qrView;
    private TextInputEditText amountEditText;
    private TextInputEditText tokenEditText;
    private ProgressBar progressBar;
    private Button qrGenerateButton;

    public ReceiveFragment() {}

    public static ReceiveFragment newInstance() {
        return new ReceiveFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_receive, container, false);

        qrView = fragmentView.findViewById(R.id.qrImageView);
        amountEditText = fragmentView.findViewById(R.id.amountEditText);
        tokenEditText = fragmentView.findViewById(R.id.tokenEditText);

        InputFilter[] editFilters =tokenEditText.getFilters();
        InputFilter[] newFilters = new InputFilter[editFilters.length + 1];
        System.arraycopy(editFilters, 0, newFilters, 0, editFilters.length);
        newFilters[editFilters.length] = new InputFilter.AllCaps();
        tokenEditText.setFilters(newFilters);

        progressBar = fragmentView.findViewById(R.id.progressBar);

        qrGenerateButton = fragmentView.findViewById(R.id.qrGenerate);
        qrGenerateButton.setOnClickListener(view -> generateQR());

        amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String token;
                if(tokenEditText.getText() == null) token = "";
                else token = tokenEditText.getText().toString();
                qrGenerateButton.setEnabled(s.length() > 0 && check(token));
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        tokenEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /*int pos = tokenEditText.getSelectionStart();
                tokenEditText.removeTextChangedListener(this);
                tokenEditText.setText(s.toString().toUpperCase());

                tokenEditText.setSelection(pos);
                tokenEditText.addTextChangedListener(this);*/
                int l;
                if(amountEditText.getText() == null) l = 0;
                else l = amountEditText.getText().length();
                qrGenerateButton.setEnabled(l > 0 && check(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });


        return fragmentView;
    }

    public boolean check(String name) {
        if(name.length() == 0) return false;
        if(name.charAt(0) == '.' || name.charAt(name.length()-1) == '.') return false;
        char[] chars = name.toCharArray();
        for (char c : chars) {
            if(!Character.isLetter(c) && c != '.') {
                return false;
            }
        }
        return true;
    }


    private void generateQR() {
        qrView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            String data = BitsharesWalletWraper.getInstance().get_account().name + "'" + amountEditText.getText().toString() + "'" + tokenEditText.getText().toString();
            QRCodeWriter writer = new QRCodeWriter();
            try {
                BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 1000, 1000);
                int width = bitMatrix.getWidth();
                int height = bitMatrix.getHeight();
                Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.parseColor("#263d70") : Color.WHITE);
                    }
                }
                new Handler(Looper.getMainLooper()).post(() -> {
                    qrView.setImageBitmap(bmp);
                    qrView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                });
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
