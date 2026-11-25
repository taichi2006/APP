package com.example.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Address implements Parcelable {
    private String id;
    private String addressTitle;
    private String fullName;
    private String street;
    private String phoneNumb;
    private String city;
    private String state;

    public Address() {
        this("", "", "", "", "", "");
    }

    public Address(String addressTitle, String fullName, String street, String phoneNumb, String city, String state) {
        this.id = "";
        this.addressTitle = addressTitle != null ? addressTitle : "";
        this.fullName = fullName != null ? fullName : "";
        this.street = street != null ? street : "";
        this.phoneNumb = phoneNumb != null ? phoneNumb : "";
        this.city = city != null ? city : "";
        this.state = state != null ? state : "";
    }

    protected Address(Parcel in) {
        id = in.readString();
        addressTitle = in.readString();
        fullName = in.readString();
        street = in.readString();
        phoneNumb = in.readString();
        city = in.readString();
        state = in.readString();
    }

    public static final Creator<Address> CREATOR = new Creator<Address>() {
        @Override
        public Address createFromParcel(Parcel in) {
            return new Address(in);
        }

        @Override
        public Address[] newArray(int size) {
            return new Address[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id != null ? id : "");
        dest.writeString(addressTitle);
        dest.writeString(fullName);
        dest.writeString(street);
        dest.writeString(phoneNumb);
        dest.writeString(city);
        dest.writeString(state);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddressTitle() {
        return addressTitle;
    }

    public void setAddressTitle(String addressTitle) {
        this.addressTitle = addressTitle;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPhoneNumb() {
        return phoneNumb;
    }

    public void setPhoneNumb(String phoneNumb) {
        this.phoneNumb = phoneNumb;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
