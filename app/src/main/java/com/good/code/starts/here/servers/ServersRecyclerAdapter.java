package com.good.code.starts.here.servers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bitshares.bitshareswallet.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServersRecyclerAdapter extends RecyclerView.Adapter<ServersRecyclerAdapter.ServerViewHolder> {

    private Context context;
    private List<Server> servers;
    private OnDeleteListener listener;
    private OnItemClickListener itemListener;

    private String current;

    private ExecutorService pingExecutor = Executors.newFixedThreadPool(3);

    public ServersRecyclerAdapter(Context context, List<Server> servers) {
        this.context = context;
        this.servers = servers;
    }

    public void setDeleteListener(OnDeleteListener listener) {
        this.listener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemListener = listener;
    }

    public void setCurrent(String address) {
        this.current = address;
    }

    public void setNewData(List<Server> newServers) {
        ServerDiffCallback diffCallback = new ServerDiffCallback(servers, newServers);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.servers = new ArrayList<>(newServers);
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public ServerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_server, parent, false);
        return new ServerViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ServerViewHolder holder, int position) {
        if(position == getItemCount() - 1) {
            holder.itemView.setVisibility(View.INVISIBLE);
        } else {
            Server server = servers.get(position);

            holder.name.setText(server.getName() + " (" + server.getAddress() + ")");
            if (server.getAddress().equals(current)) {
                holder.name.setTypeface(null, Typeface.BOLD);
            }

            holder.delete.setOnClickListener(view -> {
                if (listener != null) listener.onDelete(server);
            });

            pingExecutor.submit(() -> {
                try {
                    String address = server.getAddress();
                    address = address.replace("wss://", "");
                    int index = address.indexOf('/');
                    address = address.substring(0, index);
                    String ping = Ping.ping(address);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (ping == null) {

                            holder.ping.setText(R.string.error);
                            holder.ping.setTextColor(ContextCompat.getColor(context, R.color.ping_fail));
                        } else {
                            holder.ping.setText(ping + " ms");
                            holder.ping.setTextColor(ContextCompat.getColor(context, R.color.ping_result));
                        }
                    });
                } catch (IOException | InterruptedException e) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        holder.ping.setText(R.string.error);
                        holder.ping.setTextColor(ContextCompat.getColor(context, R.color.ping_fail));
                    });
                }
            });

            holder.itemView.setOnClickListener(view -> {
                if (itemListener != null) {
                    itemListener.onItemClick(server);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return servers.size() + 1;
    }

    static class ServerViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView ping;
        ImageView delete;

        ServerViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.serverName);
            ping = itemView.findViewById(R.id.ping);
            delete = itemView.findViewById(R.id.delete);
        }
    }

    interface OnDeleteListener {
        void onDelete(Server server);
    }

    interface OnItemClickListener {
        void onItemClick(Server server);
    }

}
