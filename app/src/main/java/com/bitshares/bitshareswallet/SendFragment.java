package com.bitshares.bitshareswallet;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bitshares.bitshareswallet.room.BitsharesAssetObject;
import com.bitshares.bitshareswallet.room.BitsharesBalanceAsset;
import com.bitshares.bitshareswallet.viewmodel.SendViewModel;
import com.bitshares.bitshareswallet.wallet.BitsharesWalletWraper;
import com.bitshares.bitshareswallet.wallet.account_object;
import com.bitshares.bitshareswallet.wallet.asset;
import com.bitshares.bitshareswallet.wallet.common.ErrorCode;
import com.bitshares.bitshareswallet.wallet.exception.ErrorCodeException;
import com.bitshares.bitshareswallet.wallet.exception.NetworkStatusException;
import com.bitshares.bitshareswallet.wallet.fc.crypto.sha256_object;
import com.bitshares.bitshareswallet.wallet.graphene.chain.signed_transaction;
import com.bituniverse.utils.NumericUtil;
import com.good.code.starts.here.dialog.select.TokenSelectDialog;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;

public class SendFragment extends BaseFragment {

    private KProgressHUD mProcessHud;
    private Button mButton;
    private Button feeButton;

    @BindView(R.id.editTextTo) EditText mEditTextTo;
    @BindView(R.id.textViewToId) TextView mTextViewId;

    @BindView(R.id.qrScan) ImageView qrScan;

    @BindView(R.id.editTextQuantity) EditText mEditTextQuantitiy;

    private View mView;
    private SendViewModel viewModel;

    private List<String> symbolList;
    private asset lastFeeAsset;

    public SendFragment() {}

    private TokenSelectDialog tokenSelectDialog;

    public static SendFragment newInstance() {
        return new SendFragment();
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_send, container, false);
        ButterKnife.bind(this, mView);

        viewModel = ViewModelProviders.of(this).get(SendViewModel.class);

        EditText editTextFrom = mView.findViewById(R.id.editTextFrom);

        String strName = BitsharesWalletWraper.getInstance().get_account().name;
        editTextFrom.setText(strName);

        sha256_object.encoder encoder = new sha256_object.encoder();
        encoder.write(strName.getBytes());

        WebView webViewFrom = mView.findViewById(R.id.webViewAvatarFrom);
        loadWebView(webViewFrom, 40, encoder.result().toString());

        TextView textView = mView.findViewById(R.id.textViewFromId);
        String strId = String.format(
                Locale.ENGLISH, "#%d",
                BitsharesWalletWraper.getInstance().get_account().id.get_instance()
        );
        textView.setText(strId);

        mProcessHud = KProgressHUD.create(getActivity())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please Wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);

        mView.findViewById(R.id.btn_send).setOnClickListener(v -> processSendClick(mView));

        mEditTextTo.setOnFocusChangeListener((v, hasFocus) -> {
            final String strText = mEditTextTo.getText().toString();
            if (!hasFocus) {
                processGetTransferToId(strText, mTextViewId);
            }
        });

        final WebView webViewTo = mView.findViewById(R.id.webViewAvatarTo);
        mEditTextTo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                sha256_object.encoder encoder = new sha256_object.encoder();
                encoder.write(s.toString().getBytes());
                loadWebView(webViewTo, 40, encoder.result().toString());
            }
        });

        qrScan.setOnClickListener(v -> {
            startActivityForResult(new Intent(getActivity(), ScannerActivity.class), ScannerActivity.REQUEST_CODE);
        });

        mEditTextQuantitiy.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                return;
            }

            String strQuantity = mEditTextQuantitiy.getText().toString();
            if (TextUtils.isEmpty(strQuantity)) {
                return;
            }

            double quantity = NumericUtil.parseDouble(strQuantity, -1.0D);
            if (Double.compare(quantity, 0.0D) < 0) {
                mEditTextQuantitiy.setText("0");
                // TODO toast
                return;
            }

            processCalculateFee();
        });

        tokenSelectDialog = new TokenSelectDialog(getActivity());

        feeButton = mView.findViewById(R.id.btn_fee_unit);

        feeButton.setOnClickListener(v -> tokenSelectDialog.show(symbolList, token -> {
            feeButton.setText(token);
            processCalculateFee();
            tokenSelectDialog.close();
        }));
        /*feeSpinner = mView.findViewById(R.id.spinner_fee_unit);

        feeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                processCalculateFee();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });*/



        mButton = mView.findViewById(R.id.btn_unit);

        mButton.setOnClickListener(v -> tokenSelectDialog.show(symbolList, token -> {
            mButton.setText(token);
            tokenSelectDialog.close();
        }));
        //mSpinner = mView.findViewById(R.id.spinner_unit);

        viewModel.getBalancesList().observe(this, bitsharesBalanceAssetList -> {
            symbolList = new ArrayList<>();
            symbolList.add("FINTEH");
            for (BitsharesBalanceAsset bitsharesBalanceAsset : bitsharesBalanceAssetList) {
                if (!bitsharesBalanceAsset.quote.equals("FINTEH"))
                    symbolList.add(bitsharesBalanceAsset.quote);
            }

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    getActivity(),
                    android.R.layout.simple_spinner_item,
                    symbolList
            );

            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            //mSpinner.setAdapter(arrayAdapter);
            //feeSpinner.setAdapter(arrayAdapter);
        });

        return mView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ScannerActivity.REQUEST_CODE && resultCode == RESULT_OK) {
            String scanData = data.getStringExtra("DATA");
            Toast.makeText(getActivity(), scanData, Toast.LENGTH_SHORT).show();
            String[] splited = scanData.split("'");
            mEditTextTo.setText(splited[0]);
            mEditTextQuantitiy.setText(splited[1]);
            int index = symbolList.indexOf(splited[2]);
            if(index >= 0) {
                mButton.setText(symbolList.get(index));
                feeButton.setText(symbolList.get(index));
                //mSpinner.setSelection(index);
                //feeSpinner.setSelection(index);
            } else {
                Toast.makeText(getActivity(), R.string.no_req_token, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onShow() {
        super.onShow();
        if (mEditTextTo.getText().length() > 0) {
            processGetTransferToId(mEditTextTo.getText().toString(), mTextViewId);
        }
        notifyUpdate();
    }

    @Override
    public void onHide() {
        super.onHide();
        hideSoftKeyboard(mEditTextTo, getActivity());
    }

    private void processTransfer(final String strFrom,
                                 final String strTo,
                                 final String strQuantity,
                                 final String strSymbol,
                                 final String strMemo) {
        mProcessHud.show();

        Flowable.just(0)
                .subscribeOn(Schedulers.io())
                .map(integer -> {
                    signed_transaction signedTransaction = BitsharesWalletWraper.getInstance().transfer(
                            strFrom,
                            strTo,
                            strQuantity,
                            strSymbol,
                            strMemo, lastFeeAsset
                    );
                    return signedTransaction;
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(signedTransaction -> {
                    if (getActivity() != null && getActivity().isFinishing() == false) {
                        mProcessHud.dismiss();
                        if (signedTransaction != null) {
                            Toast.makeText(getActivity(), "Sent Successfully", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getActivity(), "Fail to send", Toast.LENGTH_LONG).show();
                        }
                    }

                }, throwable -> {
                    if (throwable instanceof NetworkStatusException) {
                        throwable.printStackTrace();
                        if (getActivity() != null && getActivity().isFinishing() == false) {
                            mProcessHud.dismiss();
                            Toast.makeText(getActivity(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        throw Exceptions.propagate(throwable);
                    }
                });
    }

    private void processSendClick(final View view) {
        if(lastFeeAsset == null) {
            Toast.makeText(getActivity(), R.string.fee_first, Toast.LENGTH_SHORT).show();
            return;
        }
        if (BitsharesWalletWraper.getInstance().is_locked()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if(prefs.contains("pass")) {
                BitsharesWalletWraper.getInstance().unlock(prefs.getString("pass", ""));
                String strFrom = ((EditText) view.findViewById(R.id.editTextFrom)).getText().toString();
                String strTo = ((EditText) view.findViewById(R.id.editTextTo)).getText().toString();
                String strQuantity = ((EditText) view.findViewById(R.id.editTextQuantity)).getText().toString();
                String strSymbol = (String)mButton.getText();
                String strMemo = ((EditText)view.findViewById(R.id.editTextMemo)).getText().toString();
                processTransfer(strFrom, strTo, strQuantity, strSymbol, strMemo);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater layoutInflater = getActivity().getLayoutInflater();
                final View viewGroup = layoutInflater.inflate(R.layout.dialog_password_confirm, null);
                builder.setPositiveButton(
                        R.string.password_confirm_button_confirm,
                        null);

                builder.setNegativeButton(
                        R.string.password_confirm_button_cancel, null);
                builder.setView(viewGroup);
                final AlertDialog dialog = builder.create();
                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    EditText editText = viewGroup.findViewById(R.id.editTextPassword);
                    String strPassword = editText.getText().toString();
                    int nRet = BitsharesWalletWraper.getInstance().unlock(strPassword);
                    if (nRet == 0) {
                        dialog.dismiss();
                        String strFrom = ((EditText) view.findViewById(R.id.editTextFrom)).getText().toString();
                        String strTo = ((EditText) view.findViewById(R.id.editTextTo)).getText().toString();
                        String strQuantity = ((EditText) view.findViewById(R.id.editTextQuantity)).getText().toString();
                        String strSymbol = (String) mButton.getText();
                        String strMemo = ((EditText) view.findViewById(R.id.editTextMemo)).getText().toString();
                        processTransfer(strFrom, strTo, strQuantity, strSymbol, strMemo);
                    } else {
                        viewGroup.findViewById(R.id.textViewPasswordInvalid).setVisibility(View.VISIBLE);
                    }
                });
            }

        } else {
            String strFrom = ((EditText) view.findViewById(R.id.editTextFrom)).getText().toString();
            String strTo = ((EditText) view.findViewById(R.id.editTextTo)).getText().toString();
            String strQuantity = ((EditText) view.findViewById(R.id.editTextQuantity)).getText().toString();
            String strSymbol = (String)mButton.getText();
            String strMemo = ((EditText)view.findViewById(R.id.editTextMemo)).getText().toString();

            processTransfer(strFrom, strTo, strQuantity, strSymbol, strMemo);
        }
    }

    private void processGetTransferToId(final String strAccount, final TextView textViewTo) {
        Flowable.just(strAccount)
                .subscribeOn(Schedulers.io())
                .map(accountName -> {
                    account_object accountObject = BitsharesWalletWraper.getInstance().get_account_object(accountName);
                    if (accountObject == null) {
                        throw new ErrorCodeException(ErrorCode.ERROR_NO_ACCOUNT_OBJECT, "it can't find the account");
                    }

                    return accountObject;
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(accountObject -> {
                    if (getActivity() != null && getActivity().isFinishing() == false) {
                        textViewTo.setText("#" + accountObject.id.get_instance());
                    }
                }, throwable -> {
                    if (throwable instanceof NetworkStatusException || throwable instanceof ErrorCodeException) {
                        if (getActivity() != null && getActivity().isFinishing() == false) {
                            textViewTo.setText("#none");
                        }
                    } else {
                        throw Exceptions.propagate(throwable);
                    }
                });
    }

    public static void hideSoftKeyboard(View view, Context context) {
        if (view != null && context != null) {
            InputMethodManager imm = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    public static void showSoftKeyboard(View view, Context context) {
        if (view != null && context != null) {
            InputMethodManager imm = (InputMethodManager) context
                    .getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, 0);
        }
    }

    @Override
    public void notifyUpdate() {

    }

    private void processCalculateFee() {
        final String strQuantity = ((EditText) mView.findViewById(R.id.editTextQuantity)).getText().toString();
        final String strSymbol = (String) mButton.getText();
        final String strMemo = ((EditText) mView.findViewById(R.id.editTextMemo)).getText().toString();
        final String strFeeAsset = (String) feeButton.getText();

        // 用户没有任何货币，这个symbol会为空，则会出现崩溃，进行该处理进行规避
        if (TextUtils.isEmpty(strQuantity) || TextUtils.isEmpty(strSymbol)) {
            return;
        }

        Flowable.just(0)
                .subscribeOn(Schedulers.io())
                .map(integer -> {
                    asset fee = BitsharesWalletWraper.getInstance().transfer_calculate_fee(
                            strQuantity,
                            strSymbol,
                            strMemo, strFeeAsset
                    );
                    lastFeeAsset = fee;
                    BitsharesAssetObject assetObject = BitsharesApplication.getInstance()
                            .getBitsharesDatabase().getBitsharesDao().queryAssetObjectById(fee.asset_id.toString());
                    return new Pair<>(fee, assetObject);
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(resultPair -> {
                    if (getActivity() != null && getActivity().isFinishing() == false) {
                        processDisplayFee(resultPair.first, resultPair.second);
                    }
                }, throwable -> {
                    if (throwable instanceof NetworkStatusException) {
                        if (getActivity() != null && getActivity().isFinishing() == false) {
                            EditText editTextFee = mView.findViewById(R.id.editTextFee);
                            editTextFee.setText("N/A");
                        }
                    } else {
                        throw Exceptions.propagate(throwable);
                    }
                });

        /*new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    asset fee = BitsharesWalletWraper.getInstance().transfer_calculate_fee(
                            strQuantity,
                            strSymbol,
                            strMemo
                    );

                    BitsharesAssetObject assetObject = BitsharesApplication.getInstance()
                            .getBitsharesDatabase().getBitsharesDao().queryAssetObjectById(fee.asset_id.toString());



                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (getActivity() != null && getActivity().isFinishing() == false) {
                                processDisplayFee(legibleObject);
                            }
                        }
                    });
                } catch (NetworkStatusException e) {
                    e.printStackTrace();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (getActivity() != null && getActivity().isFinishing() == false) {
                                EditText editTextFee = (EditText) mView.findViewById(R.id.editTextFee);
                                editTextFee.setText("N/A");
                            }
                        }
                    });
                }
            }
        }).start();*/
    }

    private void processDisplayFee(asset fee, BitsharesAssetObject assetObject) {
        //Toast.makeText(getActivity(), assetObject.symbol + " " + fee.amount + " " + assetObject.precision, Toast.LENGTH_SHORT).show();
        EditText editTextFee = mView.findViewById(R.id.editTextFee);
        String strResult = String.format(
                Locale.ENGLISH,
                "%s (%s)",
                new BigDecimal(fee.amount).setScale((int) Math.log10(assetObject.precision), RoundingMode.UNNECESSARY).divide(new BigDecimal(assetObject.precision), RoundingMode.UNNECESSARY).toPlainString(),
                "Cannot be modified"
        );
        editTextFee.setText(strResult);
        /*spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                processCalculateFee();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });*/

        /*viewModel.getBalancesList().observe(this, bitsharesBalanceAssetList -> {
            List<String> symbolList = new ArrayList<>();
            for (BitsharesBalanceAsset bitsharesBalanceAsset : bitsharesBalanceAssetList) {
                symbolList.add(bitsharesBalanceAsset.quote);

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                        getActivity(),
                        android.R.layout.simple_spinner_item,
                        symbolList
                );

                if (mSpinner != null) {
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    feeSpinner.setAdapter(arrayAdapter);
                }
            }
        });*/

        /*List<String> listSymbols = new ArrayList<>();
        listSymbols.add(assetObject.symbol);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                listSymbols
        );*/

        /*if (mSpinner != null) {
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(arrayAdapter);
        }*/
    }

    private void loadWebView(WebView webView, int size, String encryptText) {
        String htmlShareAccountName = "<html><head><style>body,html {margin:0; padding:0; text-align:center;}</style><meta name=viewport content=width=" + size + ",user-scalable=no/></head><body><canvas width=" + size + " height=" + size + " data-jdenticon-hash=" + encryptText + "></canvas><script src=https://cdn.jsdelivr.net/jdenticon/1.3.2/jdenticon.min.js async></script></body></html>";
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadData(htmlShareAccountName, "text/html", "UTF-8");
    }

    public void processDonate(String strName, int nAmount, String strUnit) {
        if (isAdded()) {
            mEditTextTo.setText(strName);
            mEditTextQuantitiy.setText(Integer.toString(nAmount));
            //mSpinner.setSelection(0);
            mButton.setText("BTS");
        }
    }
}
