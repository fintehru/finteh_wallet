package com.good.code.starts.here.pairs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.bitshares.bitshareswallet.BitsharesApplication;
import com.bitshares.bitshareswallet.R;

public class PairsFragment extends Fragment {

    public static PairsFragment newInstance() {
        return new PairsFragment();
    }

    private PairsRecyclerAdapter adapter;

    private RecyclerView recyclerView;
    private FloatingActionButton fabAddPair;

    private SharedPreferences preferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.servers_fragment, container, false);

        preferences = PreferenceManager.getDefaultSharedPreferences(BitsharesApplication.getInstance());

        recyclerView = fragmentView.findViewById(R.id.recyclerView);
        fabAddPair = fragmentView.findViewById(R.id.addServer);

        fabAddPair.setOnClickListener(view -> {
            View dialogView = inflater.inflate(R.layout.dialog_add_server, null);
            EditText first = dialogView.findViewById(R.id.editText);
            first.setHint(R.string.first_asset);
            EditText second = dialogView.findViewById(R.id.editText2);
            second.setHint(R.string.second_asset);

            new AlertDialog.Builder(PairsFragment.this.getActivity())
                    .setTitle(R.string.add_pair)
                    .setView(dialogView)
                    .setPositiveButton(R.string.add, (dialogInterface, i) -> {
                        String firstStr = first.getText().toString();
                        String secondStr = second.getText().toString();
                        if(!check(firstStr) || !check(secondStr) || first.equals(second)) {
                            Toast.makeText(getActivity(), R.string.add_pair_err, Toast.LENGTH_SHORT).show();
                        } else {
                            adapter.add(firstStr.toUpperCase() + ":" + secondStr.toUpperCase());
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create().show();
        });

        adapter = new PairsRecyclerAdapter(getActivity());
       /* adapter.setCurrent(new FullNodeServerSelect().getServer());
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
        });*/

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);


        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        recyclerView.setAdapter(adapter);

        //repository.get().observe(getActivity(), adapter::setNewData);

        return fragmentView;
    }

    private boolean check(String name) {
        if(name.length() == 0) return false;
        if(name.charAt(0) == '.' || name.charAt(name.length()-1) == '.') return false;
        char[] chars = name.toCharArray();
        for (char c : chars) {
            if(!Character.isLetter(c) && c != '.') {
                return false;
            }
        }
        return true;
    }

}
