package com.example.core.util;

import com.example.model.Address;
import com.example.model.CartProduct;
import com.example.model.Order;
import com.example.model.Payment;
import com.example.model.Product;
import com.example.model.Review;
import com.example.model.Voucher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseSampleData {

    public static List<Product> getSampleProducts() {
        int darkGray = 0xFF2B2B2B;
        int beige = 0xFFE0D8B0;
        int woodBrown = 0xFF8B5A2B;
        int navyBlue = 0xFF1B2A4A;
        int oliveGreen = 0xFF556B2F;

        List<Product> list = new ArrayList<>();

        Product p1 = new Product("sofa_01", "Sofa Băng Nỉ Hiện Đại", "Sofa", "models/sofa_living.glb", 4500000f, 0.15f,
                "Sofa băng 3 chỗ bọc vải nỉ cao cấp dệt sợi Bỉ thoáng khí kháng khuẩn, đệm mút D40 bọt biển đúc nguyên khối.",
                Arrays.asList(darkGray, navyBlue, beige), Arrays.asList("1m8", "2m", "2m2"),
                Arrays.asList("file:///android_asset/product_images/sofa_living.png", "file:///android_asset/product_images/sofa_glam.png"));
        p1.setSoldCount(142); p1.setRating(4.9f); p1.setReviewCount(36);
        list.add(p1);

        Product p2 = new Product("sofa_02", "Sofa Nhung Glam Velvet", "Sofa", "models/sofa_glam.glb", 11500000f, null,
                "Sofa bọc nhung Glam Velvet thượng lưu mềm mại như tơ tằm, đệm ngồi 3 lớp lông vũ kết hợp mút cao cấp.",
                Arrays.asList(woodBrown, darkGray), Arrays.asList("2m6 x 1m6", "2m8 x 1m8"),
                Arrays.asList("file:///android_asset/product_images/sofa_glam.png", "file:///android_asset/product_images/sofa_living.png"));
        p2.setSoldCount(89); p2.setRating(5.0f); p2.setReviewCount(24);
        list.add(p2);

        Product p3 = new Product("chair_01", "Ghế Bành Thư Giãn Armchair", "Chair", "models/chair_armchair.glb", 2800000f, 0.1f,
                "Ghế bành thư giãn Armchair chế tác công thái học hoàn hảo với góc ngả 105 độ nâng đỡ trọn vẹn cột sống.",
                Arrays.asList(oliveGreen, beige, navyBlue), Arrays.asList("Tiêu chuẩn"),
                Arrays.asList("file:///android_asset/product_images/chair_armchair.png", "file:///android_asset/product_images/chair_sheen.png"));
        p3.setSoldCount(210); p3.setRating(4.8f); p3.setReviewCount(48);
        list.add(p3);

        Product p4 = new Product("chair_02", "Ghế Bọc Nỉ Bắc Âu Sheen Chair", "Chair", "models/chair_sheen.glb", 3200000f, null,
                "Ghế đơn phong cách Bắc Âu Scandinavian bọc nỉ Sheen Cashmere mịn màng, phối viền chỉ khâu thủ công.",
                Arrays.asList(woodBrown, beige), Arrays.asList("Tiêu chuẩn"),
                Arrays.asList("file:///android_asset/product_images/chair_sheen.png", "file:///android_asset/product_images/chair_armchair.png"));
        p4.setSoldCount(115); p4.setRating(4.9f); p4.setReviewCount(29);
        list.add(p4);

        Product p5 = new Product("table_01", "Bàn Ăn Gỗ Tự Nhiên Dining Table", "Table", "models/table_dining.glb", 5600000f, 0.2f,
                "Bàn ăn gia đình Dining Table từ 100% gỗ óc chó Bắc Mỹ tự nhiên với những đường vân cuộn xoáy quyến rũ.",
                Arrays.asList(woodBrown, darkGray), Arrays.asList("1m4 x 80cm", "1m6 x 90cm"),
                Arrays.asList("file:///android_asset/product_images/table_dining.png", "file:///android_asset/product_images/table_coffee.png"));
        p5.setSoldCount(178); p5.setRating(4.9f); p5.setReviewCount(42);
        list.add(p5);

        Product p6 = new Product("table_02", "Bàn Trà Sofa Coffee Table", "Table", "models/table_coffee.glb", 1950000f, null,
                "Bàn trà Coffee Table thiết kế thanh lịch. Mặt đá nung kết vân mây cẩm thạch chống trầy xước và ố vàng.",
                Arrays.asList(woodBrown, darkGray), Arrays.asList("Đường kính 60cm", "Đường kính 80cm"),
                Arrays.asList("file:///android_asset/product_images/table_coffee.png", "file:///android_asset/product_images/table_dining.png"));
        p6.setSoldCount(92); p6.setRating(4.7f); p6.setReviewCount(19);
        list.add(p6);

        Product p7 = new Product("bed_01", "Giường Ngủ Đôi Cao Cấp Luxury Bed", "Bed", "models/bed_luxury.glb", 9800000f, 0.1f,
                "Giường ngủ đôi hoàng gia Luxury Bed chế tác từ gỗ sồi tự nhiên nguyên tấm vững chãi chịu tải trên 500kg.",
                Arrays.asList(beige, woodBrown), Arrays.asList("1m6 x 2m", "1m8 x 2m"),
                Arrays.asList("file:///android_asset/product_images/bed_luxury.png", "file:///android_asset/product_images/bed_day.png"));
        p7.setSoldCount(164); p7.setRating(5.0f); p7.setReviewCount(52);
        list.add(p7);

        Product p8 = new Product("bed_02", "Giường Ngủ Thư Giãn Daybed", "Bed", "models/bed_day.glb", 6500000f, null,
                "Giường đơn thư giãn Daybed phong cách Quiet Luxury tối giản, tích hợp đệm bọc vải lanh Linen thoáng mát.",
                Arrays.asList(woodBrown), Arrays.asList("1m2 x 2m", "1m4 x 2m"),
                Arrays.asList("file:///android_asset/product_images/bed_day.png", "file:///android_asset/product_images/bed_luxury.png"));
        p8.setSoldCount(76); p8.setRating(4.8f); p8.setReviewCount(18);
        list.add(p8);

        Product p9 = new Product("cupboard_01", "Tủ Gỗ Vintage Cổ Điển", "Cupboard", "models/cabinet_vintage.glb", 5800000f, 0.12f,
                "Tủ lưu trữ gỗ Vintage phong cách Mid-Century cổ điển với 6 ngăn kéo trượt êm ái trang bị ray giảm chấn.",
                Arrays.asList(woodBrown, darkGray), Arrays.asList("1m2 x 90cm", "1m5 x 90cm"),
                Arrays.asList("file:///android_asset/product_images/cabinet_vintage.png", "file:///android_asset/product_images/cupboard_shelves.png"));
        p9.setSoldCount(130); p9.setRating(4.9f); p9.setReviewCount(31);
        list.add(p9);

        Product p10 = new Product("cupboard_02", "Kệ Trưng Bày Đa Tầng Shelves", "Cupboard", "models/cupboard_shelves.glb", 3200000f, null,
                "Kệ trưng bày đa tầng Shelves thiết kế bất đối xứng hiện đại, tối ưu diện tích trưng bày sách và đồ trang trí.",
                Arrays.asList(darkGray, woodBrown), Arrays.asList("1m2 x 1m8"),
                Arrays.asList("file:///android_asset/product_images/cupboard_shelves.png", "file:///android_asset/product_images/cabinet_vintage.png"));
        p10.setSoldCount(84); p10.setRating(4.8f); p10.setReviewCount(22);
        list.add(p10);

        return list;
    }

    public static List<Review> getSampleReviews() {
        List<Review> list = new ArrayList<>();
        String[] pIds = {"sofa_01", "sofa_02", "chair_01", "chair_02", "table_01", "table_02", "bed_01", "bed_02", "cupboard_01", "cupboard_02"};
        for (String pid : pIds) {
            list.add(new Review("rev_" + pid + "_1", pid, "1001", "Trần Thu Trang", "trang@gmail.com", 5.0f,
                    "Sản phẩm thực tế đẹp vượt mong đợi! Chất gỗ và đường chỉ may rất tỉ mỉ, nhìn vô cùng sang trọng.", "18/11/2026"));
            list.add(new Review("rev_" + pid + "_2", pid, "1002", "Nguyễn Minh Đức", "duc@gmail.com", 5.0f,
                    "Giao hàng đúng hẹn, đóng gói cẩn thận. Nhân viên hỗ trợ bê lên tầng và lắp đặt chu đáo.", "15/11/2026"));
            list.add(new Review("rev_" + pid + "_3", pid, "1003", "Phạm Quốc Bảo", "bao@gmail.com", 4.8f,
                    "Rất ưng ý với chất lượng hoàn thiện của TCFurnish. Xứng đáng với giá trị bỏ ra!", "10/11/2026"));
        }
        return list;
    }

    public static List<Voucher> getSampleVouchers() {
        List<Voucher> list = new ArrayList<>();
        list.add(new Voucher("TCFURNISH10", "Ưu đãi khách hàng mới - Giảm 10%", 0.10f, 0f, 0f, 500000f, "Áp dụng giảm 10% tối đa 500.000đ cho mọi đơn hàng nội thất"));
        list.add(new Voucher("VIP20", "Tri ân VIP - Giảm 20%", 0.20f, 0f, 2000000f, 2000000f, "Giảm 20% tối đa 2.000.000đ cho đơn hàng từ 2.000.000đ"));
        list.add(new Voucher("NOITHAT100K", "Voucher Nội Thất - Giảm 100.000đ", 0f, 100000f, 500000f, 100000f, "Giảm ngay 100.000đ cho đơn hàng từ 500.000đ"));
        list.add(new Voucher("AROOM50", "Voucher Quà Tặng - Giảm 50.000đ", 0f, 50000f, 200000f, 50000f, "Giảm ngay 50.000đ cho đơn hàng bất kỳ"));
        return list;
    }

    public static List<Order> getSampleOrders() {
        List<Order> list = new ArrayList<>();
        List<Product> products = getSampleProducts();

        Address addr1 = new Address("Nhà riêng", "Nguyễn Văn An", "12 Nguyễn Huệ, P. Bến Nghé", "0901234567", "Quận 1", "TP. Hồ Chí Minh");
        List<CartProduct> cp1 = new ArrayList<>();
        if (!products.isEmpty()) {
            cp1.add(new CartProduct(products.get(0), 1, 0xFF1B2A4A, "2m"));
        }
        Order o1 = new Order("Đã giao hàng", 4500000f, cp1, addr1, "20/11/2026 14:30", 10001L, "Cổng Sandbox VNPAY (Trực tuyến)", "Đã thanh toán (Mã GD: VNPAY_982341)");
        o1.setNote("Giao giờ hành chính");
        list.add(o1);

        Address addr2 = new Address("Công ty", "Trần Thị Mai", "88 Lê Lợi, P. Bến Thành", "0912345678", "Quận 1", "TP. Hồ Chí Minh");
        List<CartProduct> cp2 = new ArrayList<>();
        if (products.size() > 4) {
            cp2.add(new CartProduct(products.get(4), 1, 0xFF8B5A2B, "1m6 x 90cm"));
        }
        Order o2 = new Order("Đang giao hàng", 5600000f, cp2, addr2, "23/11/2026 09:15", 10002L, "Thanh toán khi nhận hàng (COD)", "Chờ thu tiền");
        o2.setNote("Gọi trước khi giao 15 phút");
        list.add(o2);

        return list;
    }

    public static List<Payment> getSamplePayments() {
        List<Payment> list = new ArrayList<>();
        list.add(new Payment("PAY_10001", 10001L, "USR_DEMO_01", "an.nguyen@gmail.com", 4500000.0,
                "Sandbox (VNPAY)", "SUCCESS", "TXN_982341", "NCB", "Thanh toán đơn hàng #10001"));
        list.add(new Payment("PAY_10002", 10002L, "USR_DEMO_02", "mai.tran@gmail.com", 5600000.0,
                "COD", "PENDING", "COD_10002", "CASH", "Thanh toán khi nhận hàng #10002"));
        return list;
    }
}
