package com.bitshares.bitshareswallet;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

public class ConfirmOrderDialog {
    private Activity mActivity;
    private AlertDialog.Builder mDialogBuilder;
    private AlertDialog mDialog;
    private OnDialogInterationListener mListener;

    public static class ConfirmOrderData {
        public ConfirmOrderData() {

        }

        public ConfirmOrderData(String operationName,
                                String price,
                                String quantity,
                                String total,
                                String timeExpiration,
                                String free,
                                String feeType,
                                String quantityType,
                                String totalType) {
            this.operationName = operationName;
            this.price = price;
            this.quantity = quantity;
            this.total = total;
            this.timeExpiration = timeExpiration;
            this.free = free;
            this.feeType = feeType;
            this.quantityType = quantityType;
            this.totalType = totalType;
        }

        public String getOperationName() {
            return operationName;
        }

        public void setOperationName(String operationName) {
            this.operationName = operationName;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getQuantity() {
            return quantity;
        }

        public void setQuantity(String quantity) {
            this.quantity = quantity;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        public String getTimeExpiration() {
            return timeExpiration;
        }

        public void setTimeExpiration(String timeExpiration) {
            this.timeExpiration = timeExpiration;
        }

        public String getFree() {
            return free;
        }

        public void setFree(String free) {
            this.free = free;
        }

        private String operationName;
        private String price;
        private String quantity;
        private String total;
        private String timeExpiration;
        private String free;
        private String feeType;
        private String quantityType;


        private String totalType;

        public String getQuantityType() {
            return quantityType;
        }

        public void setQuantityType(String quantityType) {
            this.quantityType = quantityType;
        }

        public String getTotalType() {
            return totalType;
        }

        public void setTotalType(String totalType) {
            this.totalType = totalType;
        }

        public String getFeeType() {
            return feeType;
        }

        public void setFeeType(String feeType) {
            this.feeType = feeType;
        }
    }

    public ConfirmOrderDialog(Activity mActivity, ConfirmOrderData confirmOrderData) {
        this.mActivity = mActivity;

        mDialogBuilder = new AlertDialog.Builder(mActivity);
        mDialogBuilder.setTitle(R.string.label_please_confirm);

        View view = mActivity.getLayoutInflater().inflate(R.layout.dialog_confirm_order, null);

        TextView tokenText = view.findViewById(R.id.dco_txt_target_coin_name);
        tokenText.setText(confirmOrderData.getQuantityType() + ":");

        TextView txtOperation = view.findViewById(R.id.dco_txt_operation);
        txtOperation.setText(confirmOrderData.getOperationName());

        TextView txtPrice2 = view.findViewById(R.id.dco_txt_price2);
        txtPrice2.setText(confirmOrderData.getTotalType() + "/" + confirmOrderData.getQuantityType());

        TextView txtPrice = view.findViewById(R.id.dco_txt_price);
        txtPrice.setText(confirmOrderData.getPrice());

        TextView txtSrcCoin = view.findViewById(R.id.dco_txt_src_coin);
        txtSrcCoin.setText(confirmOrderData.getTotal());

        //TextView txtSrcCoinName = (TextView)view.findViewById(R.id.dco_txt_src_coin_name);
        //txtSrcCoinName.setText(confirmOrderData.getTotalType() + ":");

        TextView txtTargetCoin = view.findViewById(R.id.dco_txt_target_coin);
        txtTargetCoin.setText(confirmOrderData.getQuantity());

        //TextView txtTargetCoinName = (TextView)view.findViewById(R.id.dco_txt_target_coin_name);
        //txtTargetCoinName.setText(confirmOrderData.getQuantityType() + ":");

        TextView txtExpiration = view.findViewById(R.id.dco_txt_expiration);
        txtExpiration.setText(confirmOrderData.getTimeExpiration());

        TextView txtFee = view.findViewById(R.id.dco_txt_fee);
        txtFee.setText(confirmOrderData.getFree() + " " + confirmOrderData.getFeeType());

        TextView txtConfirm = view.findViewById(R.id.dco_txt_confirm);
        txtConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                if(mListener != null){
                    mListener.onConfirm();
                }
            }
        });

        TextView txtNo = view.findViewById(R.id.dco_txt_no);
        txtNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                if(mListener != null){
                    mListener.onReject();
                }
            }
        });
        mDialogBuilder.setView(view);
    }

    public void show(){
        mDialog = mDialogBuilder.show();
    }

    public void setListener(OnDialogInterationListener listener){
        mListener = listener;
    }

    public interface OnDialogInterationListener {
        void onConfirm();
        void onReject();
    }
}
