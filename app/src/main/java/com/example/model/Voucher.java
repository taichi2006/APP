package com.example.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model ánh xạ bảng 'vouchers' trên Cloud Firestore
 */
public class Voucher implements Parcelable {

    private String code;
    private String title;
    private float discountPercentage;
    private float fixedDiscountAmount;
    private float minOrderAmount;
    private float maxDiscountAmount;
    private String description;

    public Voucher() {
        this.code = "";
        this.title = "";
        this.discountPercentage = 0f;
        this.fixedDiscountAmount = 0f;
        this.minOrderAmount = 0f;
        this.maxDiscountAmount = 0f;
        this.description = "";
    }

    public Voucher(String code, String title, float discountPercentage, float fixedDiscountAmount, float minOrderAmount, float maxDiscountAmount, String description) {
        this.code = code != null ? code : "";
        this.title = title != null ? title : "";
        this.discountPercentage = discountPercentage;
        this.fixedDiscountAmount = fixedDiscountAmount;
        this.minOrderAmount = minOrderAmount;
        this.maxDiscountAmount = maxDiscountAmount;
        this.description = description != null ? description : "";
    }

    protected Voucher(Parcel in) {
        code = in.readString();
        title = in.readString();
        discountPercentage = in.readFloat();
        fixedDiscountAmount = in.readFloat();
        minOrderAmount = in.readFloat();
        maxDiscountAmount = in.readFloat();
        description = in.readString();
    }

    public static final Creator<Voucher> CREATOR = new Creator<Voucher>() {
        @Override
        public Voucher createFromParcel(Parcel in) {
            return new Voucher(in);
        }

        @Override
        public Voucher[] newArray(int size) {
            return new Voucher[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(code);
        dest.writeString(title);
        dest.writeFloat(discountPercentage);
        dest.writeFloat(fixedDiscountAmount);
        dest.writeFloat(minOrderAmount);
        dest.writeFloat(maxDiscountAmount);
        dest.writeString(description);
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public float getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(float discountPercentage) { this.discountPercentage = discountPercentage; }

    public float getFixedDiscountAmount() { return fixedDiscountAmount; }
    public void setFixedDiscountAmount(float fixedDiscountAmount) { this.fixedDiscountAmount = fixedDiscountAmount; }

    public float getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(float minOrderAmount) { this.minOrderAmount = minOrderAmount; }

    public float getMaxDiscountAmount() { return maxDiscountAmount; }
    public void setMaxDiscountAmount(float maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
