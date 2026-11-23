package com.example.data;

public abstract class Category {
    private final String category;

    public Category(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public static final Category Chair = new Category("Chair") {};
    public static final Category Cupboard = new Category("Cupboard") {};
    public static final Category Table = new Category("Table") {};
    public static final Category Sofa = new Category("Sofa") {};
    public static final Category Bed = new Category("Bed") {};
}
