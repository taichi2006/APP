package com.example.data.order;

import android.os.Parcel;
import android.os.Parcelable;
import com.example.data.Address;
import com.example.data.CartProduct;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Order implements Parcelable {
    private String orderStatus;
    private float totalPrice;
    private List<CartProduct> products;
    private Address address;
    private String date;
    private long orderId;
    private String paymentMethod;
    private String paymentStatus;

    public Order() {
        this.orderStatus = "";
        this.totalPrice = 0f;
        this.products = new ArrayList<>();
        this.address = new Address();
        this.date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        this.orderId = 100000L + (long) (new Random().nextDouble() * 899999L);
        this.paymentMethod = "COD";
        this.paymentStatus = "Chờ xử lý";
    }

    public Order(String orderStatus, float totalPrice, List<CartProduct> products, Address address, String paymentMethod, String paymentStatus) {
        this();
        this.orderStatus = orderStatus != null ? orderStatus : "";
        this.totalPrice = totalPrice;
        this.products = products != null ? products : new ArrayList<>();
        this.address = address != null ? address : new Address();
        this.paymentMethod = paymentMethod != null ? paymentMethod : "COD";
        this.paymentStatus = paymentStatus != null ? paymentStatus : "Chờ xử lý";
    }

    public Order(String orderStatus, float totalPrice, List<CartProduct> products, Address address, String date, long orderId, String paymentMethod, String paymentStatus) {
        this.orderStatus = orderStatus != null ? orderStatus : "";
        this.totalPrice = totalPrice;
        this.products = products != null ? products : new ArrayList<>();
        this.address = address != null ? address : new Address();
        this.date = date != null ? date : new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        this.orderId = orderId;
        this.paymentMethod = paymentMethod != null ? paymentMethod : "COD";
        this.paymentStatus = paymentStatus != null ? paymentStatus : "Chờ xử lý";
    }

    protected Order(Parcel in) {
        orderStatus = in.readString();
        totalPrice = in.readFloat();
        products = in.createTypedArrayList(CartProduct.CREATOR);
        if (products == null) {
            products = new ArrayList<>();
        }
        address = in.readParcelable(Address.class.getClassLoader());
        if (address == null) {
            address = new Address();
        }
        date = in.readString();
        orderId = in.readLong();
        paymentMethod = in.readString();
        paymentStatus = in.readString();
    }

    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(orderStatus);
        dest.writeFloat(totalPrice);
        dest.writeTypedList(products);
        dest.writeParcelable(address, flags);
        dest.writeString(date);
        dest.writeLong(orderId);
        dest.writeString(paymentMethod);
        dest.writeString(paymentStatus);
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public float getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(float totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<CartProduct> getProducts() {
        return products;
    }

    public void setProducts(List<CartProduct> products) {
        this.products = products;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
