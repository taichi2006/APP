package com.example.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Model ánh xạ bảng 'payments' trên Cloud Firestore
 */
public class Payment implements Parcelable {

    private String paymentId;
    private long orderId;
    private String userId;
    private String userEmail;
    private double amount;
    private String paymentMethod; // "COD" hoặc "VNPAY"
    private String status;        // "SUCCESS", "PENDING", "FAILED"
    private String transactionNo; // Mã giao dịch ngân hàng / VNPay
    private String bankCode;      // VNPAY, NCB, VISA...
    private String orderInfo;
    private Date createdAt;

    public Payment() {
        this.createdAt = new Date();
    }

    public Payment(String paymentId, long orderId, String userId, String userEmail, double amount,
                   String paymentMethod, String status, String transactionNo, String bankCode,
                   String orderInfo) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.transactionNo = transactionNo;
        this.bankCode = bankCode;
        this.orderInfo = orderInfo;
        this.createdAt = new Date();
    }

    protected Payment(Parcel in) {
        paymentId = in.readString();
        orderId = in.readLong();
        userId = in.readString();
        userEmail = in.readString();
        amount = in.readDouble();
        paymentMethod = in.readString();
        status = in.readString();
        transactionNo = in.readString();
        bankCode = in.readString();
        orderInfo = in.readString();
        long tmpDate = in.readLong();
        createdAt = tmpDate != -1 ? new Date(tmpDate) : null;
    }

    public static final Creator<Payment> CREATOR = new Creator<Payment>() {
        @Override
        public Payment createFromParcel(Parcel in) {
            return new Payment(in);
        }

        @Override
        public Payment[] newArray(int size) {
            return new Payment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(paymentId);
        dest.writeLong(orderId);
        dest.writeString(userId);
        dest.writeString(userEmail);
        dest.writeDouble(amount);
        dest.writeString(paymentMethod);
        dest.writeString(status);
        dest.writeString(transactionNo);
        dest.writeString(bankCode);
        dest.writeString(orderInfo);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTransactionNo() { return transactionNo; }
    public void setTransactionNo(String transactionNo) { this.transactionNo = transactionNo; }

    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }

    public String getOrderInfo() { return orderInfo; }
    public void setOrderInfo(String orderInfo) { this.orderInfo = orderInfo; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
