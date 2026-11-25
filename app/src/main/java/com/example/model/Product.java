package com.example.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Product implements Parcelable {
    private String id;
    private String name;
    private String model;
    private String category;
    private float price;
    private Float offerPercentage;
    private String description;
    private List<Integer> colors;
    private List<String> sizes;
    private List<String> images;
    private int soldCount;
    private float rating = 4.9f;
    private int reviewCount = 28;

    public Product() {
        this("0", "", "", null, 0f, null, null, null, null, new ArrayList<String>());
    }

    public Product(String id, String name, String category, float price, List<String> images) {
        this(id, name, category, null, price, null, null, null, null, images);
    }

    public Product(String id, String name, String category, String model, float price, Float offerPercentage, String description, List<Integer> colors, List<String> sizes, List<String> images) {
        this.id = id != null ? id : "0";
        this.name = name != null ? name : "";
        this.category = category != null ? category : "";
        this.model = model;
        this.price = price;
        this.offerPercentage = offerPercentage;
        this.description = description;
        this.colors = colors;
        this.sizes = sizes;
        this.images = images != null ? images : new ArrayList<String>();
    }

    protected Product(Parcel in) {
        id = in.readString();
        name = in.readString();
        model = in.readString();
        category = in.readString();
        price = in.readFloat();
        offerPercentage = in.readByte() == 0 ? null : in.readFloat();
        description = in.readString();
        if (in.readByte() == 1) {
            colors = new ArrayList<>();
            in.readList(colors, Integer.class.getClassLoader());
        } else {
            colors = null;
        }
        sizes = in.createStringArrayList();
        images = in.createStringArrayList();
        if (images == null) images = new ArrayList<>();
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override public Product createFromParcel(Parcel in) { return new Product(in); }
        @Override public Product[] newArray(int size) { return new Product[size]; }
    };

    @Override public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(model);
        dest.writeString(category);
        dest.writeFloat(price);
        if (offerPercentage == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeFloat(offerPercentage);
        }
        dest.writeString(description);
        if (colors == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeList(colors);
        }
        dest.writeStringList(sizes);
        dest.writeStringList(images);
    }

    public Product copy(String newModel) {
        return new Product(id, name, category, newModel, price, offerPercentage, description, colors, sizes, images);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public float getPrice() { return price; }
    public void setPrice(float price) { this.price = price; }
    public Float getOfferPercentage() { return offerPercentage; }
    public void setOfferPercentage(Float offerPercentage) { this.offerPercentage = offerPercentage; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Integer> getColors() { return colors; }
    public void setColors(List<Integer> colors) { this.colors = colors; }
    public List<String> getSizes() { return sizes; }
    public void setSizes(List<String> sizes) { this.sizes = sizes; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public int getSoldCount() { return soldCount; }
    public void setSoldCount(int soldCount) { this.soldCount = soldCount; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
