package com.bitshares.bitshareswallet;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bitshares.bitshareswallet.room.BitsharesBalanceAsset;
import com.bitshares.bitshareswallet.viewmodel.WalletViewModel;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WalletFragment extends BaseFragment {
    private BtsFragmentPageAdapter mWalletFragmentPageAdapter;

    private SendFragment mSendFragment;

    @BindView(R.id.fw_viewPager) ViewPager mViewPager;
    @BindView(R.id.tabLayout) TabLayout mTabLayout;

    public WalletFragment() {
        // Required empty public constructor
    }

    public void onNewIntent(Intent intent){
        String strAction = intent.getStringExtra("action");
        if (!TextUtils.isEmpty(strAction)) {
            mViewPager.setCurrentItem(0);
            String strName = intent.getStringExtra("name");
            int nAmount = Integer.valueOf(intent.getStringExtra("amount"));
            String strUnit = intent.getStringExtra("unit");
            mSendFragment.processDonate(strName, nAmount, strUnit);
        }
    }

    public static WalletFragment newInstance() {
        return new WalletFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        WalletViewModel walletViewModel = ViewModelProviders.of(getActivity()).get(WalletViewModel.class);
        walletViewModel.getBalanceData().observe(
                this, resourceBalanceList -> {
                    switch (resourceBalanceList.status) {
                        case ERROR:
                            processError();
                            break;
                        case SUCCESS:
                            processShowdata(resourceBalanceList.data);
                            break;
                        case LOADING:
                            if (resourceBalanceList.data != null && resourceBalanceList.data.size() != 0) {
                                processShowdata(resourceBalanceList.data);
                            }
                            break;
                    }
                });
    }

    @Override
    public void onShow() {
        super.onShow();
    }

    @Override
    public void onHide() {
        super.onHide();
    }

    void processShowdata(List<BitsharesBalanceAsset> bitsharesBalanceAssetList) {
        /*long totalBTS = 0;
        long totalBalance = 0;
        for (BitsharesBalanceAsset bitsharesBalanceAsset : bitsharesBalanceAssetList) {
            totalBTS += bitsharesBalanceAsset.total;
            totalBalance += bitsharesBalanceAsset.balance;
        }

        if (bitsharesBalanceAssetList.isEmpty() == false) {
            BitsharesBalanceAsset bitsharesBalanceAsset = bitsharesBalanceAssetList.get(0);
            double exchangeRate = (double) totalBalance / bitsharesBalanceAsset.currency_precision / totalBTS * bitsharesBalanceAsset.base_precision;
            totalBTS /= bitsharesBalanceAssetList.get(0).base_precision;
            totalBalance /= bitsharesBalanceAssetList.get(0).currency_precision;

            String strTotalCurrency = String.format(
                    Locale.ENGLISH,
                    "= %d %s (%.4f %s/%s)",
                    totalBalance,
                    bitsharesBalanceAsset.currency,
                    exchangeRate,
                    "FINTEH",
                    bitsharesBalanceAsset.currency
            );

            textViewCurency.setText(strTotalCurrency);
        }*/

       /* String strTotalBalance = String.format(Locale.ENGLISH, "%d %s", totalBTS, "FINTEH");
        textViewBalances.setText(strTotalBalance);
        textViewCurency.setVisibility(View.VISIBLE);*/

        mWalletFragmentPageAdapter.notifyUpdate();
    }

    void processError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton(R.string.connect_fail_dialog_retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                WalletViewModel walletViewModel = ViewModelProviders.of(getActivity()).get(WalletViewModel.class);
                walletViewModel.retry();
            }
        });
        builder.setMessage(R.string.connect_fail_message);
        builder.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);
        ButterKnife.bind(this, view);

        mTabLayout.setBackgroundColor(PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("color", Color.parseColor("#303F9F")));

        Resources res = getResources();
        mWalletFragmentPageAdapter = new BtsFragmentPageAdapter(getFragmentManager(), true);
        mSendFragment = SendFragment.newInstance();
        mWalletFragmentPageAdapter.addFragment(mSendFragment, res.getString(R.string.tab_send));
        mWalletFragmentPageAdapter.addFragment(TransactionsFragment.newInstance(), res.getString(R.string.tab_transactions));
        mWalletFragmentPageAdapter.addFragment(BalancesFragment.newInstance(), res.getString(R.string.tab_balances));
        mWalletFragmentPageAdapter.addFragment(ReceiveFragment.newInstance(), res.getString(R.string.tab_receive));

        mViewPager.setAdapter(mWalletFragmentPageAdapter);
        initPager(mViewPager, mWalletFragmentPageAdapter);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() != 2) {
                    MainActivity.hideSoftKeyboard(mTabLayout, getActivity());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.getTabAt(0).setIcon(R.drawable.ic_send);
        mTabLayout.getTabAt(1).setIcon(R.drawable.ic_history);
        mTabLayout.getTabAt(2).setIcon(R.drawable.ic_wallet);
        mTabLayout.getTabAt(3).setIcon(R.drawable.ic_qrcode);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(mWalletFragmentPageAdapter.getTitle(tab.getPosition()));
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });

        return view;
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
