package com.good.code.starts.here.dialog.keys;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bitshares.bitshareswallet.R;

import java.util.List;

public class KeysAdapter extends RecyclerView.Adapter<KeysAdapter.KeyViewHolder> {

    private List<Pair<String, String>> keys;
    private ClipboardManager clipboard;
    private Context context;

    public KeysAdapter(Context context, List<Pair<String, String>> keys) {
        this.context = context;
        this.clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        this.keys = keys;
    }

    @NonNull
    @Override
    public KeyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_keys, viewGroup, false);
        return new KeyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KeyViewHolder holder, int i) {
        Pair<String, String> keyPair = keys.get(i);
        holder.textViewPub.setText(keyPair.first);
        holder.textViewPriv.setText(keyPair.second);

        holder.copyPub.setOnClickListener(v -> {
            ClipData clip = ClipData.newPlainText("PUBLIC KEY", keyPair.first);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, context.getString(R.string.pub_copied), Toast.LENGTH_SHORT).show();
        });

        holder.copyPriv.setOnClickListener(v -> {
            ClipData clip = ClipData.newPlainText("PRIVATE KEY", keyPair.second);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, context.getString(R.string.priv_copied), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return keys.size();
    }

    static class KeyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewPub;
        TextView textViewPriv;
        ImageView copyPub;
        ImageView copyPriv;

        public KeyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPub = itemView.findViewById(R.id.pubTextView);
            textViewPriv = itemView.findViewById(R.id.privTextView);
            copyPub = itemView.findViewById(R.id.pubCopy);
            copyPriv = itemView.findViewById(R.id.privCopy);
        }
    }
}
