package com.good.code.starts.here.deposit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bitshares.bitshareswallet.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class DepositRecyclerAdapter extends RecyclerView.Adapter<DepositRecyclerAdapter.ViewHolder> {

    private List<DepositCoin> depositCoinList;
    private OnItemClickListener listener;
    private Context context;

    public DepositRecyclerAdapter(Context context, DepositCoin[] depositCoinList, OnItemClickListener listener) {
        this.context = context;
        this.depositCoinList = new ArrayList<>();
        for(DepositCoin depositCoin : depositCoinList) {
            if(depositCoin.isDepositAllowed()) {
                this.depositCoinList.add(depositCoin);
            }
        }
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_depositcoin, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        DepositCoin depositCoin = depositCoinList.get(i);

        viewHolder.symbolTextView.setText(depositCoin.getName());
        viewHolder.descriptionTextView.setText(depositCoin.getDescription());
        BigDecimal min = new BigDecimal(depositCoin.getMinAmount()).setScale(depositCoin.getPrecision(), RoundingMode.UNNECESSARY).divide(new BigDecimal( Math.pow(10, depositCoin.getPrecision())), RoundingMode.UNNECESSARY);
        viewHolder.minAmountTextView.setText(context.getString(R.string.min_amount) + " " + min.stripTrailingZeros().toPlainString() + " " + depositCoin.getSymbol());

        viewHolder.itemView.setOnClickListener(v -> listener.onItemClick(depositCoin));
    }

    @Override
    public int getItemCount() {
        return depositCoinList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView symbolTextView;
        TextView descriptionTextView;
        TextView minAmountTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            symbolTextView = itemView.findViewById(R.id.symbolTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            minAmountTextView = itemView.findViewById(R.id.minAmountTextView);
        }
    }

    interface OnItemClickListener {
        void onItemClick(DepositCoin depositCoin);
    }

}
