package com.good.code.starts.here.dialog;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bitshares.bitshareswallet.R;

import java.util.ArrayList;
import java.util.List;

public class TokenSelectAdapter extends RecyclerView.Adapter<TokenSelectAdapter.TokenViewHolder> {

    private List<String> tokens;
    private List<String> back;
    private OnTokenSelectListener listener;

    public TokenSelectAdapter(List<String> tokens) {
        this.tokens = tokens;
        this.back = new ArrayList<>(tokens);
    }

    public void setListener(OnTokenSelectListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TokenViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_token, viewGroup, false);
        return new TokenViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TokenViewHolder tokenViewHolder, int i) {
        tokenViewHolder.textView.setText(tokens.get(i));

        tokenViewHolder.itemView.setOnClickListener(v -> {
            if(listener != null) listener.onSelect(tokens.get(tokenViewHolder.getAdapterPosition()));
        });
    }

    @Override
    public int getItemCount() {
        return tokens.size();
    }

    static class TokenViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        public TokenViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.itemText);
        }
    }

    public void filter(String text) {
        tokens.clear();
        if(text.isEmpty()){
            tokens.addAll(back);
        } else{
            text = text.toLowerCase();
            for(String item: back){
                if(item.toLowerCase().contains(text)){
                    tokens.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public interface OnTokenSelectListener {

        public void onSelect(String token);

    }

}
