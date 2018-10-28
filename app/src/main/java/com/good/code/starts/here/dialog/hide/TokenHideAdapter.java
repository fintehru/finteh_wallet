package com.good.code.starts.here.dialog.hide;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bitshares.bitshareswallet.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TokenHideAdapter extends RecyclerView.Adapter<TokenHideAdapter.TokenViewHolder> {

    private List<String> tokens;
    private SharedPreferences preferences;
    private Set<String> hidden;

    public TokenHideAdapter(Context context, List<String> tokens) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.hidden = preferences.getStringSet("hidden", new HashSet<>());
        this.tokens = tokens;
        this.tokens.remove("BTS");
        this.tokens.remove("FINTEH");
    }

    @NonNull
    @Override
    public TokenViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_token_hide, viewGroup, false);
        return new TokenViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TokenViewHolder tokenViewHolder, int i) {
        tokenViewHolder.textView.setText(tokens.get(i));
        tokenViewHolder.hideImage.setImageResource(hidden.contains(tokens.get(i)) ? R.drawable.ic_eye_off : R.drawable.ic_eye);

        tokenViewHolder.hideImage.setOnClickListener(v -> {
            String token = tokens.get(tokenViewHolder.getAdapterPosition());
            if(hidden.contains(token)) {
                tokenViewHolder.hideImage.setImageResource(R.drawable.ic_eye);
                hidden.remove(token);
            } else {
                tokenViewHolder.hideImage.setImageResource(R.drawable.ic_eye_off);
                hidden.add(token);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tokens.size();
    }

    public void save() {
        preferences.edit().putStringSet("hidden", hidden).apply();
    }

    static class TokenViewHolder extends RecyclerView.ViewHolder {

        TextView textView;
        ImageView hideImage;

        public TokenViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.itemText);
            hideImage = itemView.findViewById(R.id.hideImage);
        }
    }
}
