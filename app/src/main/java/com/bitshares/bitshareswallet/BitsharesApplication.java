package com.bitshares.bitshareswallet;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import com.akexorcist.localizationactivity.core.LocalizationApplicationDelegate;
import com.bitshares.bitshareswallet.room.BitsharesDatabase;
import com.good.code.starts.here.servers.Server;
import com.good.code.starts.here.servers.ServersRepository;

import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.sentry.Sentry;
import io.sentry.android.AndroidSentryClientFactory;

//@AcraCore(buildConfigClass = BuildConfig.class, reportFormat = StringFormat.JSON)
//@AcraHttpSender(uri = "https://collector.tracepot.com/e05fd60d", httpMethod = HttpSender.Method.POST)
public class BitsharesApplication extends Application {

    LocalizationApplicationDelegate localizationDelegate = new LocalizationApplicationDelegate(this);

    private static BitsharesApplication theApp;
    private BitsharesDatabase bitsharesDatabase;
    public static BitsharesApplication getInstance() {
        return theApp;
    }

    public BitsharesApplication() {
        theApp = this;
    }

    public BitsharesDatabase getBitsharesDatabase() {
        return bitsharesDatabase;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(localizationDelegate.attachBaseContext(base));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        localizationDelegate.onConfigurationChanged(this);
    }

    @Override
    public Context getApplicationContext() {
        return localizationDelegate.getApplicationContext(super.getApplicationContext());
    }

    @Override
    public void onCreate() {
        super.onCreate();

        String sentryDsn = "https://f7a95030510e4047ae1f7463327395b4@sentry.io/1261657";
        Sentry.init(sentryDsn, new AndroidSentryClientFactory(this));

        Security.insertProviderAt(new BouncyCastleProvider(), 1);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> serversStrSet = preferences.getStringSet("servers", null);
        if(serversStrSet == null) {
            String[] serversNames = getResources().getStringArray(R.array.full_node_api_server_options);
            String[] serversAddresses = getResources().getStringArray(R.array.full_node_api_server_values);
            Set<String> serversSet = new HashSet<>();
            for(int i = 0; i < serversNames.length; i++) {
                serversSet.add(serversNames[i] + " " + serversAddresses[i]);
            }
            preferences.edit().putStringSet("servers", serversSet).apply();
        } else {
            List<Server> serverList = new ArrayList<>();

            for(String serverStr: serversStrSet) {
                int lastSpace = serverStr.lastIndexOf(' ');
                serverList.add(new Server(serverStr.substring(0, lastSpace), serverStr.substring(lastSpace+1)));
            }

            ServersRepository.INSTANCE.addServers(serverList);

        }


        bitsharesDatabase = Room.databaseBuilder(
                this,
                BitsharesDatabase.class,
                "bitshares.db"
        ).allowMainThreadQueries().build();
    }
}
