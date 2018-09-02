package com.bitshares.bitshareswallet;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;
import com.bitshares.bitshareswallet.util.Safe;
import com.bitshares.bitshareswallet.wallet.BitsharesWalletWraper;

public class PinSetFragment extends Fragment {

    public static final byte SET = 0;
    public static final byte REMOVE = 1;
    public static final byte CHANGE = 2;

    private static final String ARG_MODE = "MODE";

    private SharedPreferences preferences;

    private byte mode;
    private byte step = 0;
    private String temp;

    public PinSetFragment() {}

    public static PinSetFragment newInstance(byte mode) {
        PinSetFragment fragment = new PinSetFragment();
        Bundle args = new Bundle();
        args.putByte(ARG_MODE, mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mode = getArguments().getByte(ARG_MODE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_pin_set, container, false);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        PinLockView pinLockView = fragmentView.findViewById(R.id.pin_lock_view);
        IndicatorDots indicatorDots = fragmentView.findViewById(R.id.indicator_dots);

        pinLockView.attachIndicatorDots(indicatorDots);

        pinLockView.setPinLength(6);
        pinLockView.setShowDeleteButton(true);

        indicatorDots.setIndicatorType(IndicatorDots.IndicatorType.FIXED);

        TextView textView = fragmentView.findViewById(R.id.textView);

        switch (mode) {
            case SET:
                textView.setText(R.string.pin_enter_new);
                pinLockView.setPinLockListener(new PinLockListener() {
                    @Override
                    public void onComplete(String pin) {
                        pinLockView.resetPinLockView();
                        if(step == 0) {
                            temp = pin;
                            textView.setText(R.string.pin_confirm_new);
                            step = 1;
                        } else if(step == 1) {
                            if(pin.equals(temp)) {
                                temp = null;
                                showPassDialog(pin);
                                //preferences.edit().putString("val", Safe.encryptDecrypt(pin, new char[]{'B','I','T','S','H','A','R','E'})).apply();
                                //Toast.makeText(getActivity(), R.string.pin_save_success, Toast.LENGTH_SHORT).show();
                                //getActivity().getSupportFragmentManager().popBackStack();
                            } else {
                                Toast.makeText(getActivity(), R.string.incorrect_pin, Toast.LENGTH_SHORT).show();
                                textView.setText(R.string.pin_enter_new);
                                step = 0;
                            }
                        }
                    }

                    @Override
                    public void onEmpty() { }

                    @Override
                    public void onPinChange(int pinLength, String intermediatePin) { }
                });
                break;
            case REMOVE:
                textView.setText(R.string.pin_enter_current);
                pinLockView.setPinLockListener(new PinLockListener() {
                    @Override
                    public void onComplete(String pin) {
                        if (checkPin(pin)) {
                            preferences.edit().remove("val").apply();
                            Toast.makeText(getActivity(), R.string.pin_delete_success, Toast.LENGTH_SHORT).show();
                            getActivity().getSupportFragmentManager().popBackStack();
                        } else {
                            pinLockView.resetPinLockView();
                            Toast.makeText(getActivity(), R.string.incorrect_pin, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onEmpty() { }

                    @Override
                    public void onPinChange(int pinLength, String intermediatePin) { }
                });
                break;
            case CHANGE:
                textView.setText(R.string.pin_enter_current);
                pinLockView.setPinLockListener(new PinLockListener() {
                    @Override
                    public void onComplete(String pin) {
                        if (checkPin(pin) || step != 0) {
                            pinLockView.resetPinLockView();
                            switch (step) {
                                case 0:
                                    textView.setText(R.string.pin_enter_new);
                                    step = 1;
                                    break;
                                case 1:
                                    temp = pin;
                                    textView.setText(R.string.pin_confirm_new);
                                    step = 2;
                                    break;
                                case 2:
                                    if(pin.equals(temp)) {
                                        temp = null;
                                        showPassDialog(pin);
                                        //preferences.edit().putString("val", Safe.encryptDecrypt(pin, new char[]{'B','I','T','S','H','A','R','E'})).apply();
                                        //Toast.makeText(getActivity(), R.string.pin_edit_success, Toast.LENGTH_SHORT).show();
                                        //getActivity().getSupportFragmentManager().popBackStack();
                                    } else {
                                        Toast.makeText(getActivity(), R.string.incorrect_pin, Toast.LENGTH_SHORT).show();
                                        textView.setText(R.string.pin_enter_new);
                                        step = 1;
                                    }
                                    break;
                                default:
                                    break;
                            }

                        } else {
                            pinLockView.resetPinLockView();
                            Toast.makeText(getActivity(), R.string.incorrect_pin, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onEmpty() { }

                    @Override
                    public void onPinChange(int pinLength, String intermediatePin) { }
                });
                break;
            default:
                break;
        }

        return fragmentView;
    }

    private boolean checkPin(String pin) {
        String val = preferences.getString("val", null);
        if(val != null) {
            return Safe.encryptDecrypt(val, new char[]{'B','I','T','S','H','A','R','E'}).equals(pin);
        }
        return false;
    }

    private void showPassDialog(String pin) {

        EditText editText = new EditText(getActivity());

        editText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editText.setHint(R.string.wallet_pass);

        new AlertDialog.Builder(getActivity())
                .setView(editText)
                .setMessage("App does not store your password for security reason. Therefore you have to input current wallet password to use pin code instead password.")
                .setPositiveButton(R.string.save, (dialogInterface, i) -> {
                    int res = BitsharesWalletWraper.getInstance().unlock(editText.getText().toString());
                    if(res == 0) {
                        preferences.edit().putString("val", Safe.encryptDecrypt(pin, new char[]{'B','I','T','S','H','A','R','E'})).putString("pass", editText.getText().toString()).apply();
                        Toast.makeText(getActivity(), R.string.pin_edit_success, Toast.LENGTH_SHORT).show();
                        getActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        Toast.makeText(getActivity(), "Incorrect password", Toast.LENGTH_SHORT).show();
                        showPassDialog(pin);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create().show();

    }

}
