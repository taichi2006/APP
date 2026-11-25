package com.example.model;

public abstract class OrderStatus {
    private final String status;

    public OrderStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public static class Ordered extends OrderStatus {
        public static final Ordered INSTANCE = new Ordered();
        public static final String status = "Đã đặt";
        public Ordered() { super("Đã đặt"); }
    }

    public static class Canceled extends OrderStatus {
        public static final Canceled INSTANCE = new Canceled();
        public static final String status = "Đã hủy";
        public Canceled() { super("Đã hủy"); }
        public static OrderStatus getOrderStatus(String status) {
            return OrderStatus.getOrderStatus(status);
        }
    }

    public static class Confirmed extends OrderStatus {
        public static final Confirmed INSTANCE = new Confirmed();
        public static final String status = "Đã xác nhận";
        public Confirmed() { super("Đã xác nhận"); }
    }

    public static class Shipped extends OrderStatus {
        public static final Shipped INSTANCE = new Shipped();
        public static final String status = "Đang giao hàng";
        public Shipped() { super("Đang giao hàng"); }
    }

    public static class Delivered extends OrderStatus {
        public static final Delivered INSTANCE = new Delivered();
        public static final String status = "Đã giao hàng";
        public Delivered() { super("Đã giao hàng"); }
    }

    public static class Returned extends OrderStatus {
        public static final Returned INSTANCE = new Returned();
        public static final String status = "Đã hoàn trả";
        public Returned() { super("Đã hoàn trả"); }
    }

    public static OrderStatus getOrderStatus(String status) {
        if (status == null) return Returned.INSTANCE;
        switch (status) {
            case "Ordered":
            case "Đã đặt":
                return Ordered.INSTANCE;
            case "Confirmed":
            case "Đã xác nhận":
                return Confirmed.INSTANCE;
            case "Delivered":
            case "Đã giao hàng":
                return Delivered.INSTANCE;
            case "Shipped":
            case "Đang giao hàng":
                return Shipped.INSTANCE;
            case "Canceled":
            case "Đã hủy":
                return Canceled.INSTANCE;
            default:
                return Returned.INSTANCE;
        }
    }
}
