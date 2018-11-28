package com.good.code.starts.here.deposit;

public class DepositResponse {

    private String inputAddress;
    private String inputMemo;
    private String inputCoinType;
    private String outputAddress;
    private String outputCoinType;

    public DepositResponse(String inputAddress, String inputMemo, String inputCoinType, String outputAddress, String outputCoinType) {
        this.inputAddress = inputAddress;
        this.inputMemo = inputMemo;
        this.inputCoinType = inputCoinType;
        this.outputAddress = outputAddress;
        this.outputCoinType = outputCoinType;
    }

    public String getInputAddress() {
        return inputAddress;
    }

    public void setInputAddress(String inputAddress) {
        this.inputAddress = inputAddress;
    }

    public String getInputMemo() {
        return inputMemo;
    }

    public void setInputMemo(String inputMemo) {
        this.inputMemo = inputMemo;
    }

    public String getInputCoinType() {
        return inputCoinType;
    }

    public void setInputCoinType(String inputCoinType) {
        this.inputCoinType = inputCoinType;
    }

    public String getOutputAddress() {
        return outputAddress;
    }

    public void setOutputAddress(String outputAddress) {
        this.outputAddress = outputAddress;
    }

    public String getOutputCoinType() {
        return outputCoinType;
    }

    public void setOutputCoinType(String outputCoinType) {
        this.outputCoinType = outputCoinType;
    }
}
