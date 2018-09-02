package com.good.code.starts.here.servers;

import android.support.v7.util.DiffUtil;

import java.util.List;

public class ServerDiffCallback extends DiffUtil.Callback {

    private final List<Server> oldList;
    private final List<Server> newList;

    public ServerDiffCallback(List<Server> oldList, List<Server> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        Server oldServer = oldList.get(oldItemPosition);
        Server newServer = newList.get(newItemPosition);
        return oldServer.getAddress().equals(newServer.getAddress());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Server oldServer = oldList.get(oldItemPosition);
        Server newServer = newList.get(newItemPosition);
        return oldServer.getName().equals(newServer.getName());
    }
}
