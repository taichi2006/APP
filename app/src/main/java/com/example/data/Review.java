package com.example.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Review implements Parcelable {
    private String id;
    private String productId;
    private String orderId;
    private String userName;
    private String userEmail;
    private float rating;
    private String comment;
    private String date;

    public Review() {
        this("", "", "", "", "", 5.0f, "", "");
    }

    public Review(String id, String productId, String orderId, String userName, String userEmail, float rating, String comment, String date) {
        this.id = id != null ? id : "";
        this.productId = productId != null ? productId : "";
        this.orderId = orderId != null ? orderId : "";
        this.userName = userName != null ? userName : "Khách hàng";
        this.userEmail = userEmail != null ? userEmail : "";
        this.rating = rating;
        this.comment = comment != null ? comment : "";
        this.date = date != null ? date : "";
    }

    protected Review(Parcel in) {
        id = in.readString();
        productId = in.readString();
        orderId = in.readString();
        userName = in.readString();
        userEmail = in.readString();
        rating = in.readFloat();
        comment = in.readString();
        date = in.readString();
    }

    public static final Creator<Review> CREATOR = new Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(productId);
        dest.writeString(orderId);
        dest.writeString(userName);
        dest.writeString(userEmail);
        dest.writeFloat(rating);
        dest.writeString(comment);
        dest.writeString(date);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
