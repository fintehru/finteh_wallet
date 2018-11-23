package com.bitshares.bitshareswallet;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bitshares.bitshareswallet.market.MarketStat;
import com.bitshares.bitshareswallet.room.BitsharesAsset;
import com.bitshares.bitshareswallet.room.BitsharesAssetObject;
import com.bitshares.bitshareswallet.room.BitsharesBalanceAsset;
import com.bitshares.bitshareswallet.viewmodel.SellBuyViewModel;
import com.bitshares.bitshareswallet.wallet.BitsharesWalletWraper;
import com.bitshares.bitshareswallet.wallet.Broadcast;
import com.bitshares.bitshareswallet.wallet.asset;
import com.bitshares.bitshareswallet.wallet.exception.NetworkStatusException;
import com.bitshares.bitshareswallet.wallet.graphene.chain.asset_object;
import com.bitshares.bitshareswallet.wallet.graphene.chain.global_property_object;
import com.bitshares.bitshareswallet.wallet.graphene.chain.utils;
import com.bituniverse.network.Status;
import com.bituniverse.utils.NumericUtil;
import com.good.code.starts.here.dialog.select.TokenSelectDialog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class TransactionSellBuyFragment extends BaseFragment
        implements View.OnClickListener
        , TextWatcher
        , MarketStat.OnMarketStatUpdateListener
        , ConfirmOrderDialog.OnDialogInterationListener {

    private static final long MARKET_STAT_INTERVAL_MILLIS = TimeUnit.SECONDS.toMillis(5);
    private static final String TRANSACTION_TYPE = "transaction_type";

    private static int buyTimeSec = 5 * 365 * 24 * 60 * 60;

    private TransactionSellBuyRecyclerViewAdapter buyRecyclerViewAdapter;
    private TransactionSellBuyRecyclerViewAdapter sellRecyclerViewAdapter;
    private RecyclerView buyRecyclerView;
    private RecyclerView sellRecyclerView;
    private int maxOrderCount = 10;

    public static final int TRANSACTION_BUY = 1;
    public static final int TRANSACTION_SELL = 2;
    private int transactionType;
    private MarketStat orderStat;
    private String baseAsset;
    private String quoteAsset;
    private double lowestSellPrice = -1;
    private double higgestBuyPrice = -1;
    private OnFragmentInteractionListener mListener;

    private EditText qEditText;
    private EditText pEditText;
    private EditText tEditText;
    private EditText fEditText;

    private TextView pTextLastView;
    private TextView qTextLastView;
    private TextView tTextLastView;

    private TextView balanceText;
    private TextView askText;

    private List<String> symbolList;

    private TextView balanceTextBase;
    private TextView askTextBase;
    private TextView askTextInfo;

    private KProgressHUD mProcessHud;

    private Button feeButton;
    private asset lastFeeAsset;
    private TokenSelectDialog tokenSelectDialog;

    private asset_object btsAssetObj;
    private asset_object baseAssetObj;
    private asset_object quoteAssetObj;
    private global_property_object globalPropertyObject;
    private boolean isAssetObjIsInit;
    private boolean isInitPriceValue;

    private Gson gson = new Gson();

    NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
    DecimalFormat decimalFormat = (DecimalFormat)nf;


    public TransactionSellBuyFragment() {
        orderStat = new MarketStat();
        decimalFormat.applyPattern("#.########");
    }

    public static TransactionSellBuyFragment newInstance(int transactionType) {
        TransactionSellBuyFragment fragment = new TransactionSellBuyFragment();
        Bundle args = new Bundle();
        args.putInt(TRANSACTION_TYPE, transactionType);
        fragment.setArguments(args);
        return fragment;
    }

    private BroadcastReceiver currencyUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            orderStat.unsubscribe(baseAsset, quoteAsset);

            updateCurency();
            orderStat.subscribe(baseAsset, quoteAsset, MarketStat.STAT_MARKET_ORDER_BOOK,
                    MARKET_STAT_INTERVAL_MILLIS, TransactionSellBuyFragment.this);
        }
    };

    @Override
    public void onMarketStatUpdate(MarketStat.Stat stat) {
        if (getView() == null || stat.orderBook == null) {
            return;
        }

        if (stat.orderBook.bids != null && !stat.orderBook.bids.isEmpty()) {
            int maxCount = Math.min(stat.orderBook.bids.size(), maxOrderCount);
            buyRecyclerViewAdapter.setList(stat.orderBook.bids.subList(0, maxCount));

            higgestBuyPrice = stat.orderBook.bids.get(0).price;
        } else {
            higgestBuyPrice = -1;
        }

        if (stat.orderBook.asks != null && !stat.orderBook.asks.isEmpty()) {
            int maxCount = Math.min(stat.orderBook.asks.size(), maxOrderCount);
            sellRecyclerViewAdapter.setList(stat.orderBook.asks.subList(0, maxCount));

            lowestSellPrice = stat.orderBook.asks.get(0).price;
        } else {
            lowestSellPrice = -1;
        }

        initPriceValue();
    }

    public void initPriceValue() {

        if (!isInitPriceValue) {
            isInitPriceValue = true;
            if (transactionType == TRANSACTION_BUY && lowestSellPrice != -1) {
                pEditText.setText(decimalFormat.format(lowestSellPrice));
            } else if (transactionType == TRANSACTION_SELL && higgestBuyPrice != -1) {
                pEditText.setText(decimalFormat.format(higgestBuyPrice));
            }
        }

        if (transactionType == TRANSACTION_BUY && lowestSellPrice != -1) {
            askText.setText(decimalFormat.format(lowestSellPrice));
        } else if (transactionType == TRANSACTION_SELL && higgestBuyPrice != -1) {
            askText.setText(decimalFormat.format(higgestBuyPrice));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            transactionType = getArguments().getInt(TRANSACTION_TYPE);
        }
    }

    @Override
    public void onShow() {
        super.onShow();
        updateCurency();
        initFee();
        updateMaxOrderCount();
        orderStat.subscribe(baseAsset, quoteAsset, MarketStat.STAT_MARKET_ORDER_BOOK,
                MARKET_STAT_INTERVAL_MILLIS, this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Broadcast.CURRENCY_UPDATED);
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(currencyUpdateReceiver, intentFilter);

        SellBuyViewModel viewModel = ViewModelProviders.of(this).get(SellBuyViewModel.class);
        if (isBuy()) {
            viewModel.changeBalanceAsset(baseAsset);
        } else {
            viewModel.changeBalanceAsset(quoteAsset);
        }

        viewModel.getAvaliableBalance().observe(this, bitsharesAssetResource -> {
            if (bitsharesAssetResource.status == Status.SUCCESS) {
                BitsharesAsset bitsharesAsset = bitsharesAssetResource.data;
                if (bitsharesAsset != null) {
                    DecimalFormat decimalFormat = new DecimalFormat("#.####");
                    balanceText.setText(decimalFormat.format((double) bitsharesAsset.amount / bitsharesAsset.precision));
                } else {
                    balanceText.setText("0");
                }
            }
        });
    }

    @Override
    public void onHide() {
        super.onHide();
        orderStat.unsubscribe(baseAsset, quoteAsset);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(currencyUpdateReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buy_sell, container, false);

        Button okButton = view.findViewById(R.id.okButton);
        okButton.setOnClickListener(this);
        if (isBuy()) {
            okButton.setText(getResources().getString(R.string.title_buy));
        } else {
            okButton.setText(getResources().getString(R.string.title_sell));
        }

        mProcessHud = KProgressHUD.create(getActivity())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please Wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);

        Button restButton = view.findViewById(R.id.restButton);
        restButton.setOnClickListener(this);

        qEditText = view.findViewById(R.id.qEditText);
        pEditText = view.findViewById(R.id.pEditText);
        pEditText.addTextChangedListener(this);
        qEditText.addTextChangedListener(this);

        tokenSelectDialog = new TokenSelectDialog(getActivity());

        feeButton = view.findViewById(R.id.fTextLastView);

        ViewModelProviders.of(this).get(SellBuyViewModel.class).getBalancesList().observe(this, bitsharesBalanceAssetList -> {
            symbolList = new ArrayList<>();
            symbolList.add("FINTEH");
            for (BitsharesBalanceAsset bitsharesBalanceAsset : bitsharesBalanceAssetList) {
                if (!bitsharesBalanceAsset.quote.equals("FINTEH"))
                    symbolList.add(bitsharesBalanceAsset.quote);
            }
        });

        feeButton.setOnClickListener(v -> tokenSelectDialog.show(symbolList, token -> {
            feeButton.setText(token);

            String qString = qEditText.getText().toString();
            String pString = pEditText.getText().toString();
            if (TextUtils.isEmpty(qString) || TextUtils.isEmpty(pString)) {
                tokenSelectDialog.close();
                return;
            }

            double qValue = NumericUtil.parseDouble(qString, -1.0D);
            double pValue = NumericUtil.parseDouble(pString, -1.0D);

            if (isBuy()) {
                double fee = calculateBuyFee(quoteAssetObj, baseAssetObj, pValue, qValue);
                fEditText.setText(decimalFormat.format(fee));
            } else {
                double fee = calculateSellFee(quoteAssetObj, baseAssetObj, pValue, qValue);
                fEditText.setText(decimalFormat.format(fee));
            }

            tokenSelectDialog.close();
        }));

        tEditText = view.findViewById(R.id.tEditText);
        fEditText = view.findViewById(R.id.fEditText);
        pTextLastView = view.findViewById(R.id.pTextLastView);
        qTextLastView = view.findViewById(R.id.qTextLastView);
        tTextLastView = view.findViewById(R.id.tTextLastView);

        balanceText = view.findViewById(R.id.balanceText);
        askText = view.findViewById(R.id.askText);

        balanceTextBase = view.findViewById(R.id.balanceTextBase);
        askTextBase = view.findViewById(R.id.askTextBase);
        askTextInfo = view.findViewById(R.id.askTextInfo);

        buyRecyclerView = view.findViewById(R.id.buy_recycler);
        buyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        buyRecyclerViewAdapter = new TransactionSellBuyRecyclerViewAdapter();
        buyRecyclerView.setAdapter(buyRecyclerViewAdapter);
        buyRecyclerView.setItemAnimator(null);

        sellRecyclerView = view.findViewById(R.id.sell_recycler);
        sellRecyclerViewAdapter = new TransactionSellBuyRecyclerViewAdapter();
        sellRecyclerView.setAdapter(sellRecyclerViewAdapter);

        sellRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        sellRecyclerView.setItemAnimator(null);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
        }


    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.okButton:
                if(lastFeeAsset == null) {

                    /*String qString = qEditText.getText().toString();
                    String pString = pEditText.getText().toString();
                    if (TextUtils.isEmpty(qString) || TextUtils.isEmpty(pString)) {
                        return;
                    }
                    double qValue = NumericUtil.parseDouble(qString, -1.0D);
                    double pValue = NumericUtil.parseDouble(pString, -1.0D);

                    if (isBuy()) {
                        calculateBuyFee(quoteAssetObj, baseAssetObj, pValue, qValue);
                    } else {
                        calculateSellFee(quoteAssetObj, baseAssetObj, pValue, qValue);
                    }*/
                    Toast.makeText(getActivity(), R.string.fee_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                ConfirmOrderDialog.ConfirmOrderData confirmOrderData = new ConfirmOrderDialog.ConfirmOrderData();
                if (transactionType == TRANSACTION_BUY) {
                    confirmOrderData.setOperationName(getResources().getString(R.string.title_buy));
                } else {
                    confirmOrderData.setOperationName(getResources().getString(R.string.title_sell));
                }
                confirmOrderData.setPrice(pEditText.getText().toString());
                confirmOrderData.setQuantity(qEditText.getText().toString());
                confirmOrderData.setTotal(tEditText.getText().toString());
                confirmOrderData.setFree(fEditText.getText().toString());
                confirmOrderData.setFeeType(feeButton.getText().toString());
                confirmOrderData.setQuantityType(utils.getAssetSymbolDisply(quoteAsset));
                confirmOrderData.setTotalType(utils.getAssetSymbolDisply(baseAsset));

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 5);
                calendar.getTime();
                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                confirmOrderData.setTimeExpiration(formatter.format(calendar.getTimeInMillis()));
                ConfirmOrderDialog confirmOrderDialog = new ConfirmOrderDialog(getActivity(), confirmOrderData);
                confirmOrderDialog.setListener(this);
                confirmOrderDialog.show();
                break;
            case R.id.restButton:
                resetInputInfo();
                break;
        }
    }

    public void resetInputInfo() {
        qEditText.setText("");
        pEditText.setText("");
        tEditText.setText("");
        fEditText.setText("");

        isInitPriceValue = false;
        initPriceValue();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String qString = qEditText.getText().toString();
        String pString = pEditText.getText().toString();
        if (TextUtils.isEmpty(qString) || TextUtils.isEmpty(pString)) {
            return;
        }

        double qValue = NumericUtil.parseDouble(qString, -1.0D);
        double pValue = NumericUtil.parseDouble(pString, -1.0D);
        double total = qValue * pValue;



        tEditText.setText(decimalFormat.format(total));

        if (isBuy()) {
            double fee = calculateBuyFee(quoteAssetObj, baseAssetObj, pValue, qValue);
            fEditText.setText(decimalFormat.format(fee));
        } else {
            double fee = calculateSellFee(quoteAssetObj, baseAssetObj, pValue, qValue);
            fEditText.setText(decimalFormat.format(fee));
        }
    }

    @Override
    public void onConfirm() {
        String qString = qEditText.getText().toString();
        String pString = pEditText.getText().toString();
        if (qString.equals("") || pString.equals("")) {
            return;
        }

        final double qValue;
        final double pValue;
        try {
            qValue = Double.parseDouble(qString);
            pValue = Double.parseDouble(pString);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (qValue <= 0 || pValue <= 0) {
            return;
        }

        if (!BitsharesWalletWraper.getInstance().is_locked()) {
            if (transactionType == TRANSACTION_BUY) {
                buy(pValue, qValue, buyTimeSec);
            } else if (transactionType == TRANSACTION_SELL) {
                sell(pValue, qValue, buyTimeSec);
            }
        } else {
            TransactionSellBuyPasswordDialog builder = new TransactionSellBuyPasswordDialog(getActivity());
            builder.setListener(new TransactionSellBuyPasswordDialog.OnDialogInterationListener() {
                @Override
                public void onConfirm(AlertDialog dialog, String passwordString) {
                    if (BitsharesWalletWraper.getInstance().unlock(passwordString) == 0) {
                        dialog.dismiss();
                        if (transactionType == TRANSACTION_BUY) {
                            buy(pValue, qValue, buyTimeSec);
                        } else if (transactionType == TRANSACTION_SELL) {
                            sell(pValue, qValue, buyTimeSec);
                        }
                    } else {
                        Toast.makeText(getContext(), getContext().getString(R.string.password_invalid), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onReject(AlertDialog dialog) {
                    dialog.dismiss();
                }
            });
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if(prefs.contains("pass")) {
                if (BitsharesWalletWraper.getInstance().unlock(prefs.getString("pass", "")) == 0) {
                    if (transactionType == TRANSACTION_BUY) {
                        buy(pValue, qValue, buyTimeSec);
                    } else if (transactionType == TRANSACTION_SELL) {
                        sell(pValue, qValue, buyTimeSec);
                    }
                } else {
                    Toast.makeText(getContext(), getContext().getString(R.string.password_invalid), Toast.LENGTH_LONG).show();
                }
            } else {
                builder.show();
            }
        }
    }

    @Override
    public void onReject() {

    }

    private void initFee() {
        isAssetObjIsInit = false;
        new Thread(() -> {
            try {
                btsAssetObj = BitsharesWalletWraper.getInstance().lookup_asset_symbols("BTS");
                baseAssetObj = BitsharesWalletWraper.getInstance().lookup_asset_symbols(baseAsset);
                quoteAssetObj = BitsharesWalletWraper.getInstance().lookup_asset_symbols(quoteAsset);
                globalPropertyObject = BitsharesWalletWraper.getInstance().get_global_properties();
                getActivity().runOnUiThread(() -> isAssetObjIsInit = true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void buy(final double rate, final double amount, final int timeSec) {
        mProcessHud.show();
        new Thread(() -> {
            try {
                BitsharesWalletWraper.getInstance().buy(quoteAsset, baseAsset, rate, amount, timeSec, lastFeeAsset);
                getActivity().runOnUiThread(() -> mProcessHud.dismiss());
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> {
                    mProcessHud.dismiss();
                    try {
                        JsonObject jsonObject = gson.fromJson(e.getMessage(), JsonObject.class);
                        Toast.makeText(getActivity(), jsonObject.getAsJsonPrimitive("message").getAsString(), Toast.LENGTH_LONG).show();
                    } catch (Exception ex) {
                        Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void sell(final double rate, final double amount, final int timeSec) {
        mProcessHud.show();
        new Thread(() -> {
            try {
                BitsharesWalletWraper.getInstance().sell(quoteAsset, baseAsset, rate, amount, timeSec, lastFeeAsset);
                getActivity().runOnUiThread(() -> mProcessHud.dismiss());
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> {
                    mProcessHud.dismiss();
                    try {
                        JsonObject jsonObject = gson.fromJson(e.getMessage(), JsonObject.class);
                        Toast.makeText(getActivity(), jsonObject.getAsJsonPrimitive("message").getAsString(), Toast.LENGTH_LONG).show();
                    } catch (Exception ex) {
                        Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void updateMaxOrderCount(){
        int orderItemHeightDp = DensityUtil.dpToPx(getContext(), TransactionSellBuyRecyclerViewAdapter.ORDERITEMHEIGHTDP);
        int buyRecyclerHeight = buyRecyclerView.getHeight();

        maxOrderCount = buyRecyclerHeight / orderItemHeightDp;
        if (maxOrderCount <= 0) {
            maxOrderCount = 8;
        }
    }

    private void updateCurency(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String strAssetPair = prefs.getString("quotation_currency_pair", "FINTEH:RUDEX.BTC");
        String strAsset[] = strAssetPair.split(":");
        baseAsset = strAsset[1];
        quoteAsset = strAsset[0];

        String baseAssetDisplay = utils.getAssetSymbolDisply(baseAsset);
        String quoteAssetDisplay = utils.getAssetSymbolDisply(quoteAsset);
        pTextLastView.setText(baseAssetDisplay + "/" + quoteAssetDisplay);
        qTextLastView.setText(quoteAssetDisplay);
        tTextLastView.setText(baseAssetDisplay);
        if (isBuy()) {
            balanceTextBase.setText(baseAssetDisplay);
            askTextBase.setText(baseAssetDisplay);
            askTextInfo.setText(getString(R.string.label_lowest_ask));
            askText.setText(" ");
        } else {
            balanceTextBase.setText(quoteAssetDisplay);
            askTextBase.setText(baseAssetDisplay);
            askTextInfo.setText(getString(R.string.label_highest_bid));
            askText.setText(" ");
        }
        sellRecyclerViewAdapter.clear();
        buyRecyclerViewAdapter.clear();
    }

    private boolean isBuy(){
        return transactionType == TRANSACTION_BUY;
    }

    private double calculateSellFee(asset_object symbolToSell, asset_object symbolToReceive, double rate,
                                    double amount) {
        if (!isAssetObjIsInit) {
            return 0;
        }
        try {
            asset a = BitsharesWalletWraper.getInstance().calculate_sell_fee(symbolToSell, symbolToReceive,
                    rate, amount, globalPropertyObject, feeButton.getText().toString());
            lastFeeAsset = a;
            if (a.asset_id.equals(btsAssetObj.id)) {
                return utils.get_asset_amount(a.amount, btsAssetObj);
            } else if (a.asset_id.equals(baseAssetObj.id)) {
                return utils.get_asset_amount(a.amount, baseAssetObj);
            } else if(a.asset_id.equals(quoteAssetObj.id)) {
                return utils.get_asset_amount(a.amount, quoteAssetObj);
            } else {
                BitsharesAssetObject assetObject = BitsharesApplication.getInstance()
                        .getBitsharesDatabase().getBitsharesDao().queryAssetObjectById(a.asset_id.toString());
                return utils.get_asset_amount(a.amount, assetObject);
            }
        } catch (NetworkStatusException e) {
            return 0;
        }
    }

    private double calculateBuyFee(asset_object symbolToReceive, asset_object symbolToSell, double rate,
                                   double amount) {
        if (!isAssetObjIsInit) {
            return 0;
        }
        try {
            asset a = BitsharesWalletWraper.getInstance().calculate_buy_fee(symbolToReceive, symbolToSell,
                    rate, amount, globalPropertyObject, feeButton.getText().toString());

            lastFeeAsset = a;
            if (a.asset_id.equals(btsAssetObj.id)) {
                return utils.get_asset_amount(a.amount, btsAssetObj);
            } else if (a.asset_id.equals(baseAssetObj.id)) {
                return utils.get_asset_amount(a.amount, baseAssetObj);
            } else if(a.asset_id.equals(quoteAssetObj.id)) {
                return utils.get_asset_amount(a.amount, quoteAssetObj);
            } else {
                BitsharesAssetObject assetObject = BitsharesApplication.getInstance()
                        .getBitsharesDatabase().getBitsharesDao().queryAssetObjectById(a.asset_id.toString());
                return utils.get_asset_amount(a.amount, assetObject);
            }
        } catch (NetworkStatusException e) {
            return 0;
        }
    }
}
