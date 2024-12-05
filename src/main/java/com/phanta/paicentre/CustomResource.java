package com.phanta.paicentre;

import com.google.gson.annotations.SerializedName;

public class CustomResource {

    @SerializedName("id")
    private String id;

    @SerializedName("create_time")
    private String createTime;

    @SerializedName("update_time")
    private String updateTime;

    @SerializedName("amount")
    private CustomAmount amount;

    @SerializedName("state")
    private String state;

    @SerializedName("parent_payment")
    private String parentPayment;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public CustomAmount getAmount() {
        return amount;
    }

    public void setAmount(CustomAmount amount) {
        this.amount = amount;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getParentPayment() {
        return parentPayment;
    }

    public void setParentPayment(String parentPayment) {
        this.parentPayment = parentPayment;
    }
}
