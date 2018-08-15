package com.bitshares.bitshareswallet;

import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerFragment extends Fragment implements ZXingScannerView.ResultHandler {

    private OnResultListener listener = null;

    private boolean mFlash = false;
    private int mCameraId = 0;

    private ZXingScannerView mScannerView;
    private ImageView flashView;

    private Drawable flashOn;
    private Drawable flashOff;

    public static ScannerFragment newInstance() {
        return new ScannerFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        View fragmentView = inflater.inflate(R.layout.fragment_camera, container, false);
        mScannerView = fragmentView.findViewById(R.id.cameraView);
        flashView = fragmentView.findViewById(R.id.flashAction);

        flashOn = ContextCompat.getDrawable(getActivity(), R.drawable.flash);
        flashOff = ContextCompat.getDrawable(getActivity(), R.drawable.flash_off);

        mScannerView.setAspectTolerance(0.5f);

        List<BarcodeFormat> formats = new ArrayList<>();
        formats.add(BarcodeFormat.QR_CODE);
        if(mScannerView != null) {
            mScannerView.setFormats(formats);
        }

        mScannerView.setResultHandler(this);
        mScannerView.startCamera(mCameraId);
        mScannerView.setFlash(mFlash);
        mScannerView.setAutoFocus(true);


        fragmentView.findViewById(R.id.switchAction).setOnClickListener(v -> {
            mCameraId = ~mCameraId+2;
            mFlash = false;
            flashView.setImageDrawable(flashOff);

            mScannerView.startCamera(mCameraId);
            mScannerView.setFlash(mFlash);
            mScannerView.setAutoFocus(true);
        });


        flashView.setOnClickListener(v -> {
            mFlash = !mFlash;
            flashView.setImageDrawable(mFlash ? flashOn : flashOff);
            mScannerView.setFlash(mFlash);
        });

        return fragmentView;
    }

    public void setOnResultListener(OnResultListener listener) {
        this.listener = listener;
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera(mCameraId);
        mScannerView.setFlash(mFlash);
        mScannerView.setAspectTolerance(0.5f);
        mScannerView.setAutoFocus(true);
    }

    @Override
    public void handleResult(Result rawResult) {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getActivity().getApplicationContext(), notification);
            r.play();
            getActivity().getSupportFragmentManager().popBackStack();
        } catch (Exception ignored) {}
        if(listener != null) {
            listener.onResult(rawResult.getText());
        }
    }

    interface OnResultListener {
        void onResult(String data);
    }

}