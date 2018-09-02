package com.good.code.starts.here.servers;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.bitshares.bitshareswallet.BitsharesApplication;
import com.bitshares.bitshareswallet.R;
import com.bitshares.bitshareswallet.wallet.FullNodeServerSelect;
import com.jakewharton.processphoenix.ProcessPhoenix;

import java.util.Collections;

public class ServersFragment extends Fragment {

    public static ServersFragment newInstance() {
        return new ServersFragment();
    }

    private ServersRecyclerAdapter adapter;

    private RecyclerView recyclerView;
    private FloatingActionButton fabAddServer;

    private SharedPreferences preferences;

    private ServersRepository repository = ServersRepository.INSTANCE;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.servers_fragment, container, false);

        getActivity().setTitle(R.string.servers);

        preferences = PreferenceManager.getDefaultSharedPreferences(BitsharesApplication.getInstance());

        recyclerView = fragmentView.findViewById(R.id.recyclerView);
        fabAddServer = fragmentView.findViewById(R.id.addServer);

        fabAddServer.setOnClickListener(view -> {
            View dialogView = inflater.inflate(R.layout.dialog_add_server, null);
            EditText name = dialogView.findViewById(R.id.editText);
            EditText address = dialogView.findViewById(R.id.editText2);

            new AlertDialog.Builder(ServersFragment.this.getActivity())
                    .setTitle(R.string.add_server)
                    .setView(dialogView)
                    .setPositiveButton(R.string.add, (dialogInterface, i) -> repository.addServers(Collections.singletonList(new Server(name.getText().toString(), address.getText().toString().replace(" ", "")))))
                    .setNegativeButton(R.string.cancel, null)
                    .create().show();
        });

        adapter = new ServersRecyclerAdapter(getActivity(), repository.get().getValue());
        adapter.setCurrent(new FullNodeServerSelect().getServer());
        adapter.setDeleteListener(server -> {
            if(adapter.getItemCount() == 2) {
                Toast.makeText(getActivity(), R.string.cant_delete_last, Toast.LENGTH_SHORT).show();
            } else if(server.getAddress().equals(preferences.getString("full_node_api_server", ""))) {
                Toast.makeText(getActivity(), R.string.cant_delete, Toast.LENGTH_SHORT).show();
            } else {
                preferences.edit().putString("full_node_api_server", "autoselect").apply();
                repository.deleteServer(server);
            }
        });
        adapter.setOnItemClickListener(server -> {

            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.app_will_restart)
                    .setPositiveButton(R.string.restart_now, (dialogInterface, i) -> {
                        preferences.edit().putString("full_node_api_server", server.getAddress()).apply();
                        ProcessPhoenix.triggerRebirth(BitsharesApplication.getInstance());
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create().show();
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adapter);

        repository.get().observe(getActivity(), adapter::setNewData);

        return fragmentView;
    }

}
