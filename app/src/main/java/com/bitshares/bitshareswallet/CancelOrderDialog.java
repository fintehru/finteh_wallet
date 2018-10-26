package com.bitshares.bitshareswallet;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.bitshares.bitshareswallet.market.OpenOrder;
import com.bitshares.bitshareswallet.wallet.graphene.chain.utils;

import java.text.SimpleDateFormat;


public class CancelOrderDialog {
    private AlertDialog.Builder mDialogBuilder;
    private AlertDialog mDialog;
    private OnDialogInterationListener mListener;
    private boolean confirm = false;
    public CancelOrderDialog(Activity mActivity, OpenOrder order) {
        Activity mActivity1 = mActivity;
        mDialogBuilder = new AlertDialog.Builder(mActivity);
        mDialogBuilder.setTitle(R.string.label_confirm_cancel_order);

        View view = mActivity.getLayoutInflater().inflate(R.layout.dialog_cancel_order, null);

        TextView txtOperation = view.findViewById(R.id.dco_txt_operation);
        TextView txtPrice = view.findViewById(R.id.dco_txt_price);
        TextView txtSrcCoin = view.findViewById(R.id.dco_txt_src_coin);
        TextView txtSrcCoinName = view.findViewById(R.id.dco_txt_src_coin_name);
        TextView txtTargetCoin = view.findViewById(R.id.dco_txt_target_coin);
        TextView txtTargetCoinName = view.findViewById(R.id.dco_txt_target_coin_name);
        TextView txtExpiration = view.findViewById(R.id.dco_txt_expiration);

        if (order.limitOrder.sell_price.quote.asset_id.equals(order.quote.id)) {
            txtOperation.setText(R.string.label_buy);
            double buyAmount = utils.get_asset_amount(order.limitOrder.sell_price.quote.amount, order.quote);
            txtTargetCoin.setText(String.format("%.6f",buyAmount));

            double sellAmount = utils.get_asset_amount(order.limitOrder.sell_price.base.amount, order.base);
            txtSrcCoin.setText(String.format("%.6f",sellAmount));
        } else {
            txtOperation.setText(R.string.label_sell);
            double buyAmount = utils.get_asset_amount(order.limitOrder.sell_price.quote.amount, order.base);
            txtSrcCoin.setText(String.format("%.6f",buyAmount));

            double sellAmount = utils.get_asset_amount(order.limitOrder.sell_price.base.amount, order.quote);
            txtTargetCoin.setText(String.format("%.6f",sellAmount));
        }

        txtPrice.setText(String.format("%.6f %s/%s", order.price,order.base.symbol,order.quote.symbol ));
        txtSrcCoinName.setText(order.base.symbol + ":");
        txtTargetCoinName.setText(order.quote.symbol + ":");

        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        txtExpiration.setText(formatter.format(order.limitOrder.expiration));

        TextView txtConfirm = view.findViewById(R.id.dco_txt_confirm);
        txtConfirm.setOnClickListener(v -> {
            mDialog.dismiss();
            confirm = true;
        });

        TextView txtNo = view.findViewById(R.id.dco_txt_no);
        txtNo.setOnClickListener(v -> {
            mDialog.dismiss();
            confirm = false;
        });
        mDialogBuilder.setView(view);
    }

    public void show(){
        mDialog = mDialogBuilder.show();
        mDialog.setOnDismissListener(dialog -> {
            if(mListener != null){
                if(confirm){
                    mListener.onConfirm();
                } else {
                    mListener.onReject();
                }
            }
        });
    }

    public void setListener(OnDialogInterationListener listener){
        mListener = listener;
    }

    public interface OnDialogInterationListener {
        void onConfirm();
        void onReject();
    }
}
