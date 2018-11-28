package com.good.code.starts.here.deposit;

public class DepositCoin {

    private String name;
    private String description;
    private String backingCoin;
    private String symbol;
    private String walletType;
    private String gatewayWallet;
    private boolean depositAllowed;
    private boolean withdrawalAllowed;
    private int precision;
    private int minAmount;

    public DepositCoin(String name, String description, String backingCoin, String symbol, String walletType, String gatewayWallet, boolean depositAllowed, boolean withdrawalAllowed, int precision, int minAmount) {
        this.name = name;
        this.description = description;
        this.backingCoin = backingCoin;
        this.symbol = symbol;
        this.walletType = walletType;
        this.gatewayWallet = gatewayWallet;
        this.depositAllowed = depositAllowed;
        this.withdrawalAllowed = withdrawalAllowed;
        this.precision = precision;
        this.minAmount = minAmount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBackingCoin() {
        return backingCoin;
    }

    public void setBackingCoin(String backingCoin) {
        this.backingCoin = backingCoin;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getWalletType() {
        return walletType;
    }

    public void setWalletType(String walletType) {
        this.walletType = walletType;
    }

    public String getGatewayWallet() {
        return gatewayWallet;
    }

    public void setGatewayWallet(String gatewayWallet) {
        this.gatewayWallet = gatewayWallet;
    }

    public boolean isDepositAllowed() {
        return depositAllowed;
    }

    public void setDepositAllowed(boolean depositAllowed) {
        this.depositAllowed = depositAllowed;
    }

    public boolean isWithdrawalAllowed() {
        return withdrawalAllowed;
    }

    public void setWithdrawalAllowed(boolean withdrawalAllowed) {
        this.withdrawalAllowed = withdrawalAllowed;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public int getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(int minAmount) {
        this.minAmount = minAmount;
    }
}
