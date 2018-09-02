package com.good.code.starts.here.servers;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bitshares.bitshareswallet.BitsharesApplication;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServersRepository {

    public static final ServersRepository INSTANCE = new ServersRepository();

    private SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BitsharesApplication.getInstance());
    private MutableLiveData<List<Server>> serversLiveData;

    private ServersRepository() {
        serversLiveData = new MutableLiveData<>();
    }

    public void addServers(List<Server> servers) {

        Set<String> set = prefs.getStringSet("servers", new HashSet<>());
        for(Server s: servers) {
            set.add(s.getName() + " " + s.getAddress());
        }
        prefs.edit().putStringSet("servers", set).apply();

        List<Server> value = serversLiveData.getValue();
        if(value == null) {
            value = servers;
        } else {
            value.addAll(servers);
        }
        serversLiveData.setValue(value);
    }

    public void deleteServer(Server server) {

        Set<String> set = prefs.getStringSet("servers", new HashSet<>());
        set.remove(server.getName() + " " + server.getAddress());
        prefs.edit().putStringSet("servers", set).apply();

        List<Server> value = serversLiveData.getValue();
        for(int i = 0; i < value.size(); i++) {
            if(value.get(i).getAddress().equals(server.getAddress())) {
                value.remove(i);
                break;
            }
        }
        serversLiveData.setValue(value);
    }

    public MutableLiveData<List<Server>> get() {
        return serversLiveData;
    }
}
