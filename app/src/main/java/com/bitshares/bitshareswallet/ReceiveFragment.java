package com.bitshares.bitshareswallet;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bitshares.bitshareswallet.wallet.BitsharesWalletWraper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class ReceiveFragment extends BaseFragment {

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

        ImageView qrView = fragmentView.findViewById(R.id.qrImageView);

        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(BitsharesWalletWraper.getInstance().get_account().name, BarcodeFormat.QR_CODE, 1000, 1000);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.parseColor("#263d70") : Color.WHITE);
                }
            }
            qrView.setImageBitmap(bmp);

        } catch (WriterException e) {
            e.printStackTrace();
        }

        return fragmentView;
    }

}
