package com.bitshares.bitshareswallet;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.localizationactivity.ui.LocalizationActivity;
import com.bitshares.bitshareswallet.room.BitsharesAsset;
import com.bitshares.bitshareswallet.room.BitsharesDao;
import com.bitshares.bitshareswallet.room.BitsharesOperationHistory;
import com.bitshares.bitshareswallet.viewmodel.QuotationViewModel;
import com.bitshares.bitshareswallet.viewmodel.WalletViewModel;
import com.bitshares.bitshareswallet.wallet.BitsharesWalletWraper;
import com.bitshares.bitshareswallet.wallet.Broadcast;
import com.bitshares.bitshareswallet.wallet.account_object;
import com.bitshares.bitshareswallet.wallet.fc.crypto.sha256_object;
import com.bitshares.bitshareswallet.wallet.graphene.chain.signed_transaction;
import com.bitshares.bitshareswallet.wallet.graphene.chain.utils;
import com.good.code.starts.here.ColorUtils;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.sentry.Sentry;
import io.sentry.event.UserBuilder;


public class MainActivity extends LocalizationActivity
implements OnFragmentInteractionListener{

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public static boolean rasingColorRevers = false;

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private NonScrollViewPager mViewPager;

    private WalletFragment mWalletFragment;
    private QuotationFragment mQuotationFragment;
    private ExchangeFragment mExchangeFragment;
    private BtsFragmentPageAdapter mMainFragmentPageAdapter;
    private TextView mTxtTitle;
    private LinearLayout mLayoutTitle;
    private BottomNavigationView mBottomNavigation;

    private String lastTitle;

    private static final int REQUEST_CODE_SETTINGS = 1;

    private int color;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == REQUEST_CODE_SETTINGS && resultCode == RESULT_OK) {
            String strChanged = data.getStringExtra("setting_changed");
            if (strChanged.equals("currency_setting")) {
                WalletViewModel walletViewModel = ViewModelProviders.of(this).get(WalletViewModel.class);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BitsharesApplication.getInstance());
                String strCurrency = prefs.getString("currency_setting", "Default");
                walletViewModel.changeCurrency(strCurrency);
            }
        }
    }

    private void onCurrencyUpdate(){
        updateTitle();
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Broadcast.CURRENCY_UPDATED));
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mWalletFragment.onNewIntent(intent);
    }

    private void updateTitle(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BitsharesApplication.getInstance());
        String strCurrencySetting = prefs.getString("quotation_currency_pair", "FINTEH:RUDEX.BTC");
        String strAsset[] = strCurrencySetting.split(":");

        try {
            mTxtTitle.setText(String.format("%s : %s ",
                    utils.getAssetSymbolDisply(strAsset[0]),
                    utils.getAssetSymbolDisply(strAsset[1]))
            );
        } catch (ArrayIndexOutOfBoundsException ignored) {}
    }

    private void setTitleVisible(boolean visible){
        mLayoutTitle.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //if(preferences.contains("locale")) setLanguage(preferences.getString("locale", "ru"));

        account_object account = BitsharesWalletWraper.getInstance().get_account();
        Sentry.getContext().setUser(new UserBuilder()
                .setUsername(account.name)
                .setId(account.id.toString())
                .withData("pin", PreferenceManager.getDefaultSharedPreferences(this).contains("val"))
                .build());

        rasingColorRevers = getResources().getConfiguration().locale.getCountry().equals("CN");
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mToolbar.setTitle(getResources().getString(R.string.tab_send));

        color = ColorUtils.getMainColor(this);
        mToolbar.setBackgroundColor(color);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ColorUtils.manipulateColor(color, 0.75f));
        }

        mLayoutTitle = mToolbar.findViewById(R.id.lay_title);
        mTxtTitle = mToolbar.findViewById(R.id.txt_bar_title);
        updateTitle();
        setTitleVisible(false);

        mLayoutTitle.setOnClickListener(v -> processChooseCurency());

        mDrawerLayout = findViewById(R.id.drawer);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();

        mViewPager = findViewById(R.id.viewPager);

        mMainFragmentPageAdapter = new BtsFragmentPageAdapter(getSupportFragmentManager(), false);

        mWalletFragment = WalletFragment.newInstance();
        mQuotationFragment = QuotationFragment.newInstance();
        mExchangeFragment = ExchangeFragment.newInstance();

        mMainFragmentPageAdapter.addFragment(mWalletFragment, "Wallet");
        //mMainFragmentPageAdapter.addFragment(mQuotationFragment, "Quotation");
        mMainFragmentPageAdapter.addFragment(mExchangeFragment, "Exchange");
        mViewPager.setAdapter(mMainFragmentPageAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position == 0) {
                    mBottomNavigation.setSelectedItemId(R.id.navigation_wallet);
                    if(lastTitle != null) getSupportActionBar().setTitle(lastTitle);
                    lastTitle = null;
                } else if(position == 1) {
                    /*mBottomNavigation.setSelectedItemId(R.id.navigation_quotation);
                    if(lastTitle == null) lastTitle = mToolbar.getTitle().toString();
                    mToolbar.setTitle("");*/
                    mBottomNavigation.setSelectedItemId(R.id.navigation_exchange);
                    if (lastTitle == null) lastTitle = mToolbar.getTitle().toString();
                    mToolbar.setTitle("");
                }
                /*} else if(position == 2) {
                    mBottomNavigation.setSelectedItemId(R.id.navigation_exchange);
                    if(lastTitle == null) lastTitle = mToolbar.getTitle().toString();
                    mToolbar.setTitle("");
                }*/
                setTitleVisible(position!=0);
                mMainFragmentPageAdapter.updatePagePosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.logout:
                    processLogout();
                    break;
                case R.id.settings:
                    Intent intentSettings = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivityForResult(intentSettings, REQUEST_CODE_SETTINGS);
                    break;
                case R.id.about:
                    Intent intentAbout = new Intent(MainActivity.this, AboutActivity.class);
                    startActivity(intentAbout);
                    break;
                //case R.id.backup:
                    //BitsharesWalletWraper.getInstance().get_account().
            }

            mDrawerLayout.closeDrawer(GravityCompat.START);
            return false;
        });

        WalletViewModel walletViewModel = ViewModelProviders.of(this).get(WalletViewModel.class);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BitsharesApplication.getInstance());
        String strCurrency = prefs.getString("currency_setting", "USD");
        walletViewModel.changeCurrency(strCurrency);


        final account_object accountObject = BitsharesWalletWraper.getInstance().get_account();
        if (accountObject != null) {
            View view = navigationView.getHeaderView(0);
            view.setBackgroundColor(color);
            TextView textViewAccountName = view.findViewById(R.id.textViewAccountName);
            textViewAccountName.setText(accountObject.name);

            TextView ltmText = view.findViewById(R.id.ltm_text);
            ImageView ltmImage = view.findViewById(R.id.ltm_image);

            if(accountObject.referrer.equals(accountObject.id.toString())) {
                ltmText.setVisibility(View.VISIBLE);
                ltmImage.setVisibility(View.VISIBLE);
            } else {
                ltmText.setVisibility(View.GONE);
                ltmImage.setVisibility(View.GONE);
            }

            sha256_object.encoder encoder = new sha256_object.encoder();
            encoder.write(accountObject.name.getBytes());

            WebView webView = view.findViewById(R.id.webViewAvatar);
            loadWebView(webView, 70, encoder.result().toString());

            TextView textViewAccountId = view.findViewById(R.id.textViewAccountId);
            textViewAccountId.setText("#" + accountObject.id.get_instance());

            view.findViewById(R.id.textViewCopyAccount).setOnClickListener(v -> {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("account name", accountObject.name);
                clipboardManager.setPrimaryClip(clipData);
                Toast toast = Toast.makeText(MainActivity.this, "Copy Successfully", Toast.LENGTH_SHORT);
                toast.show();
            });
        }


        mBottomNavigation = findViewById(R.id.navigation_bottom);

        int[] colors = new int[] {Color.parseColor("#606060"), color};
        int [][] states = new int [][]{new int[] { android.R.attr.state_enabled, -android.R.attr.state_checked}, new int[] {android.R.attr.state_enabled, android.R.attr.state_checked}};

        mBottomNavigation.setItemTextColor(new ColorStateList(states, colors));
        mBottomNavigation.setItemIconTintList(new ColorStateList(states, colors));

        mBottomNavigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.navigation_wallet:
                    mViewPager.setCurrentItem(0, true);
                    return true;
                case R.id.navigation_exchange:
                    mViewPager.setCurrentItem(1, true);
                    return true;
                /*case R.id.navigation_exchange:
                    mViewPager.setCurrentItem(2, true);
                    return true;*/
            }
            return false;
        });

        QuotationViewModel viewModel = ViewModelProviders.of(this).get(QuotationViewModel.class);
        viewModel.getSelectedMarketTicker().observe(this,
                currencyPair -> {
                    mTxtTitle.setText(String.format("%s : %s ",
                            utils.getAssetSymbolDisply(currencyPair.second),
                            utils.getAssetSymbolDisply(currencyPair.first))
                    );
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMainFragmentPageAdapter.updateShowing(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMainFragmentPageAdapter.updateShowing(true);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    @Override
    public void notifyTransferComplete(signed_transaction signedTransaction) {
        // 沿用该线程，阻塞住了系统来进行数据更新
        //mWalletFragment.notifyTransferComplete(signedTransaction);
    }

    @Override
    public void notifyCurrencyPairChange() {
        onCurrencyUpdate();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
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

    private void loadWebView(WebView webView, int size, String encryptText) {
        String htmlShareAccountName = "<html><head><style>body,html { margin:0; padding:0; text-align:center;}</style><meta name=viewport content=width=" + size + ",user-scalable=no/></head><body><canvas width=" + size + " height=" + size + " data-jdenticon-hash=" + encryptText + "></canvas><script src=https://cdn.jsdelivr.net/jdenticon/1.3.2/jdenticon.min.js async></script></body></html>";
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadData(htmlShareAccountName, "text/html", "UTF-8");
    }

    private void processLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setPositiveButton(R.string.log_out_dialog_confirm_button, (dialog, which) -> {
            Flowable.just(0)
                    .subscribeOn(Schedulers.io())
                    .map(integer -> {
                        BitsharesDao bitsharesDao = BitsharesApplication.getInstance().getBitsharesDatabase().getBitsharesDao();;
                        List<BitsharesAsset> bitsharesAssetList = bitsharesDao.queryBalanceList();
                        List<BitsharesOperationHistory> bitsharesOperationHistoryList = bitsharesDao.queryOperationHistoryList();
                        bitsharesDao.deleteBalance(bitsharesAssetList);
                        bitsharesDao.deleteOperationHistory(bitsharesOperationHistoryList);

                        return 0;
                    }).subscribe();

            BitsharesWalletWraper.getInstance().reset();
            Intent intent = new Intent(MainActivity.this, SignUpButtonActivity.class);
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton(R.string.log_out_dialog_cancel_button, null);

        builder.setMessage(R.string.log_out_dialog_message);
        builder.show();
    }

    private void processChooseCurency(){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        dialogBuilder.setTitle(R.string.title_select_currency);
        Resources res = getResources();
        final String[] arrValues = res.getStringArray(R.array.quotation_currency_pair_values);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BitsharesApplication.getInstance());
        String strCurrencySetting = prefs.getString("quotation_currency_pair", "FINTEH:RUDEX.BTC");
        int currSelectIndex = 0;
        for(int i=0; i<arrValues.length; i++){
            if(arrValues[i].equals(strCurrencySetting)){
                currSelectIndex = i;
                break;
            }
        }
        dialogBuilder.setSingleChoiceItems(R.array.quotation_currency_pair_options, currSelectIndex, (dialog, which) -> {
            dialog.dismiss();
            prefs.edit().
                    putString("quotation_currency_pair", arrValues[which])
                    .apply();
            String strAsset[] = arrValues[which].split(":");
            QuotationViewModel viewModel = ViewModelProviders.of(MainActivity.this).get(QuotationViewModel.class);
            viewModel.selectedMarketTicker(new Pair(strAsset[1], strAsset[0]));

            onCurrencyUpdate();
        });

        dialogBuilder.setPositiveButton(R.string.log_out_dialog_cancel_button, null);

        dialogBuilder.show();
    }

}
