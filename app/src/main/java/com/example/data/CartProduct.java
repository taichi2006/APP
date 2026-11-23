package com.example.data;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public class CartProduct implements Parcelable {
    private Product product;
    private int quantity;
    private Integer selectedColor;
    private String selectedSize;

    public CartProduct() {
        this(new Product(), 1, null, null);
    }

    public CartProduct(Product product, int quantity) {
        this(product, quantity, null, null);
    }

    public CartProduct(Product product, int quantity, Integer selectedColor, String selectedSize) {
        this.product = product != null ? product : new Product();
        this.quantity = quantity;
        this.selectedColor = selectedColor;
        this.selectedSize = selectedSize;
    }

    protected CartProduct(Parcel in) {
        product = in.readParcelable(Product.class.getClassLoader());
        quantity = in.readInt();
        if (in.readByte() == 0) {
            selectedColor = null;
        } else {
            selectedColor = in.readInt();
        }
        selectedSize = in.readString();
    }

    public static final Creator<CartProduct> CREATOR = new Creator<CartProduct>() {
        @Override
        public CartProduct createFromParcel(Parcel in) {
            return new CartProduct(in);
        }

        @Override
        public CartProduct[] newArray(int size) {
            return new CartProduct[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(product, flags);
        dest.writeInt(quantity);
        if (selectedColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(selectedColor);
        }
        dest.writeString(selectedSize);
    }

    public CartProduct copy(int quantity) {
        return new CartProduct(this.product, quantity, this.selectedColor, this.selectedSize);
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, quantity);
    }

    public Integer getSelectedColor() {
        return selectedColor;
    }

    public void setSelectedColor(Integer selectedColor) {
        this.selectedColor = selectedColor;
    }

    public String getSelectedSize() {
        return selectedSize;
    }

    public void setSelectedSize(String selectedSize) {
        this.selectedSize = selectedSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartProduct that = (CartProduct) o;
        return Objects.equals(product != null ? product.getId() : null, that.product != null ? that.product.getId() : null) &&
                Objects.equals(selectedColor, that.selectedColor) &&
                Objects.equals(selectedSize, that.selectedSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(product != null ? product.getId() : null, selectedColor, selectedSize);
    }
}
