package com.bitshares.bitshareswallet.wallet;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.bitshares.bitshareswallet.BitsharesApplication;
import com.good.code.starts.here.servers.Server;
import com.good.code.starts.here.servers.ServersRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Created by lorne on 07/09/2017.
 */

public class FullNodeServerSelect {

    public String getServer() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(BitsharesApplication.getInstance());
        String strServer = sharedPreferences.getString("full_node_api_server", "autoselect");
        switch (strServer) {
            case "autoselect":
                String server = getAutoSelectServer();
                sharedPreferences.edit().putString("full_node_api_server", server).apply();
                Log.w("FULL NODE", server);
                return server;
            default:
                Log.w("FULL NODE", "?>" + strServer);
                return strServer;
        }
    }

    public String getAutoSelectServer() {
        List<WebSocket> listWebsocket = new ArrayList<>();
        final Object objectSync = new Object();

        List<String> serverList = new ArrayList<>();
        for(Server s: ServersRepository.INSTANCE.get().getValue()) {
            serverList.add(s.getAddress());
        }

        final int nTotalCount = serverList.size();
        final List<String> listSelectedServer = new ArrayList<>();
        for (final String strServer : serverList) {
            Request request = new Request.Builder().url(strServer).build();
            OkHttpClient okHttpClient = new OkHttpClient();
            WebSocket webSocket = okHttpClient.newWebSocket(request, new WebSocketListener() {
                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    super.onFailure(webSocket, t, response);
                    synchronized (objectSync) {
                        listSelectedServer.add(""); // 失败，则填空

                        if (listSelectedServer.size() == nTotalCount) {
                            objectSync.notify();;
                        }
                    }
                }

                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    super.onOpen(webSocket, response);
                    synchronized (objectSync) {
                        listSelectedServer.add(strServer);
                        objectSync.notify();
                    }
                }
            });
            listWebsocket.add(webSocket);
        }

        String strResultServer = "";
        synchronized (objectSync) {
            if (listSelectedServer.isEmpty() == false && listSelectedServer.size() < nTotalCount ) {
                for (String strServer : listSelectedServer) {
                    if (strServer.isEmpty() == false) {
                        strResultServer = strServer;
                        break;
                    }
                }
            }

            if (strResultServer.isEmpty()) {
                try {
                    objectSync.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (listSelectedServer.isEmpty() == false && listSelectedServer.size() < nTotalCount ) {
                    for (String strServer : listSelectedServer) {
                        if (strServer.isEmpty() == false) {
                            strResultServer = strServer;
                            break;
                        }
                    }
                }
            }
        }

        for (WebSocket webSocket : listWebsocket) {
            webSocket.close(1000, "close");
        }

        return strResultServer;
    }
}
