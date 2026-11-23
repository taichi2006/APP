package com.example.util;

import android.util.Log;
import com.example.data.Product;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseSeeder {

    private static final String TAG = "DatabaseSeeder";

    public interface OnSeedCompleteListener {
        void onComplete(boolean success, String message);
    }

    public static void seedProductsIfEmpty() {
        seedProductsIfEmpty(null);
    }

    public static void seedProductsIfEmpty(OnSeedCompleteListener onComplete) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("Products").limit(1).get()
                .addOnSuccessListener(snapshot -> {
                    boolean needsUpdate = snapshot.isEmpty();
                    if (!snapshot.isEmpty()) {
                        DocumentSnapshot firstDoc = snapshot.getDocuments().get(0);
                        Object imagesObj = firstDoc.get("images");
                        String firstImg = null;
                        if (imagesObj instanceof List) {
                            List<?> imgList = (List<?>) imagesObj;
                            if (!imgList.isEmpty() && imgList.get(0) != null) {
                                firstImg = imgList.get(0).toString();
                            }
                        }
                        String modelField = firstDoc.getString("model");
                        String desc = firstDoc.getString("description");
                        if (modelField == null || modelField.trim().isEmpty() ||
                                (firstImg != null && firstImg.contains("unsplash")) ||
                                modelField.endsWith("sofa.glb") ||
                                modelField.endsWith("arm_chair__furniture.glb") ||
                                (desc != null && desc.length() < 120)) {
                            needsUpdate = true;
                        }
                    }

                    if (needsUpdate) {
                        Log.d(TAG, "Products collection needs update with 100% matched 3D models and renders. Seeding...");
                        seedProducts(onComplete);
                    } else {
                        String msg = "Products collection already synced with matched 3D models.";
                        Log.d(TAG, msg);
                        if (onComplete != null) {
                            onComplete.onComplete(true, msg);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    String errorMsg = "Failed to query Products collection: " + e.getMessage();
                    Log.e(TAG, errorMsg);
                    if (onComplete != null) {
                        onComplete.onComplete(false, errorMsg);
                    }
                });
    }

    public static void seedProducts() {
        seedProducts(null);
    }

    public static void seedProducts(OnSeedCompleteListener onComplete) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        List<Product> products = getSampleProducts();
        WriteBatch batch = firestore.batch();

        for (Product product : products) {
            batch.set(firestore.collection("Products").document(product.getId()), product);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    String successMsg = "Successfully seeded " + products.size() + " products to Firestore with 100% matching 3D models and renders!";
                    Log.d(TAG, successMsg);
                    if (onComplete != null) {
                        onComplete.onComplete(true, successMsg);
                    }
                })
                .addOnFailureListener(e -> {
                    String errorMsg = "Failed to write products batch: " + e.getMessage();
                    Log.e(TAG, errorMsg);
                    if (onComplete != null) {
                        onComplete.onComplete(false, errorMsg);
                    }
                });
    }

    public static List<Product> getSampleProducts() {
        int darkGray = 0xFF2B2B2B;
        int beige = 0xFFE0D8B0;
        int woodBrown = 0xFF8B5A2B;
        int navyBlue = 0xFF1B2A4A;
        int oliveGreen = 0xFF556B2F;

        List<Product> list = new ArrayList<>();

        // ================= SOFA =================
        list.add(new Product(
                "sofa_01",
                "Sofa Băng Nỉ Hiện Đại",
                "Sofa",
                "models/sofa_living.glb",
                4500000f,
                0.15f,
                "Sofa băng 3 chỗ bọc vải nỉ cao cấp dệt sợi Bỉ thoáng khí kháng khuẩn, đệm mút D40 bọt biển đúc nguyên khối chống xẹp lún kết hợp hệ lò xo túi độc lập. Khung gỗ sồi tự nhiên Bắc Mỹ đã qua sấy nhiệt chống cong vênh mối mọt, thiết kế tối giản tinh tế mang lại vẻ sang trọng bền vững cho phòng khách hiện đại.",
                Arrays.asList(darkGray, navyBlue, beige),
                Arrays.asList("1m8", "2m", "2m2"),
                Arrays.asList("file:///android_asset/product_images/sofa_living.png")
        ));

        list.add(new Product(
                "sofa_02",
                "Sofa Nhung Glam Velvet",
                "Sofa",
                "models/sofa_glam.glb",
                11500000f,
                null,
                "Sofa bọc nhung Glam Velvet thượng lưu với độ óng ả mềm mại như tơ tằm, đệm ngồi 3 lớp lông vũ kết hợp mút cao cấp êm ái tuyệt hảo. Chân hợp kim phủ PVD mạ vàng Champagne bóng mờ chống oxy hóa, kiến tạo điểm nhấn hoàng gia kiêu sa cho không gian phòng khách biệt thự.",
                Arrays.asList(woodBrown, darkGray),
                Arrays.asList("2m6 x 1m6", "2m8 x 1m8"),
                Arrays.asList("file:///android_asset/product_images/sofa_glam.png")
        ));

        // ================= CHAIR =================
        list.add(new Product(
                "chair_01",
                "Ghế Bành Thư Giãn Armchair",
                "Chair",
                "models/chair_armchair.glb",
                2800000f,
                0.1f,
                "Ghế bành thư giãn Armchair chế tác công thái học hoàn hảo với góc ngả 105 độ nâng đỡ trọn vẹn cột sống. Bề mặt bọc vải dạ thô đan sợi đa sắc cao cấp kết hợp khung gỗ tần bì nguyên khối uốn nhiệt nghệ thuật, mang đến trải nghiệm nghỉ ngơi tĩnh tại và thanh lịch.",
                Arrays.asList(oliveGreen, beige, navyBlue),
                Arrays.asList("Tiêu chuẩn"),
                Arrays.asList("file:///android_asset/product_images/chair_armchair.png")
        ));

        list.add(new Product(
                "chair_02",
                "Ghế Bọc Nỉ Bắc Âu Sheen Chair",
                "Chair",
                "models/chair_sheen.glb",
                3200000f,
                null,
                "Ghế đơn phong cách Bắc Âu Scandinavian bọc nỉ Sheen Cashmere mịn màng, phối viền chỉ khâu thủ công tinh xảo của các nghệ nhân bậc thầy. Chân gỗ sồi tự nhiên FAS phủ dầu thực vật Inchem mờ dịu, toát lên vẻ đẹp mộc mạc mà sang trọng cho góc đọc sách hoặc sảnh đón.",
                Arrays.asList(woodBrown, beige),
                Arrays.asList("Tiêu chuẩn"),
                Arrays.asList("file:///android_asset/product_images/chair_sheen.png")
        ));

        // ================= TABLE =================
        list.add(new Product(
                "table_01",
                "Bàn Ăn Gỗ Tự Nhiên Dining Table",
                "Table",
                "models/table_dining.glb",
                5600000f,
                0.2f,
                "Bàn ăn gia đình Dining Table từ 100% gỗ óc chó Bắc Mỹ (FAS) tự nhiên với những đường vân cuộn xoáy quyến rũ độc bản. Mộng gỗ mộc truyền thống không dùng đinh kim loại, bề mặt phủ 5 lớp sơn Inchem chống thấm nước và chịu nhiệt độ cao tới 200°C, gắn kết trọn vẹn từng khoảnh khắc sum vầy gia đình.",
                Arrays.asList(woodBrown, darkGray),
                Arrays.asList("1m4 x 80cm", "1m6 x 90cm"),
                Arrays.asList("file:///android_asset/product_images/table_dining.png")
        ));

        list.add(new Product(
                "table_02",
                "Bàn Trà Sofa Coffee Table",
                "Table",
                "models/table_coffee.glb",
                1950000f,
                null,
                "Bàn trà Coffee Table thiết kế thanh lịch lấy cảm hứng từ triết lý hình học đương đại. Mặt đá nung kết vân mây cẩm thạch chống trầy xước và ố vàng, kết hợp khung chân gỗ tự nhiên bo cong mềm mại, tôn vinh vẻ hài hòa khi đặt cạnh những bộ sofa phòng khách cao cấp.",
                Arrays.asList(woodBrown, darkGray),
                Arrays.asList("Đường kính 60cm", "Đường kính 80cm"),
                Arrays.asList("file:///android_asset/product_images/table_coffee.png")
        ));

        // ================= BED =================
        list.add(new Product(
                "bed_01",
                "Giường Ngủ Đôi Cao Cấp Luxury Bed",
                "Bed",
                "models/bed_luxury.glb",
                9800000f,
                0.1f,
                "Giường ngủ đôi hoàng gia Luxury Bed chế tác từ gỗ sồi tự nhiên nguyên tấm vững chãi với khả năng chịu tải trên 500kg. Tựa đầu giường bọc da bò Ý cao cấp êm ái cùng hệ thống nan cong giảm chấn công nghệ châu Âu, nâng niu giấc ngủ sâu và tái tạo năng lượng hoàn hảo.",
                Arrays.asList(beige, woodBrown),
                Arrays.asList("1m6 x 2m", "1m8 x 2m"),
                Arrays.asList("file:///android_asset/product_images/bed_luxury.png")
        ));

        list.add(new Product(
                "bed_02",
                "Giường Ngủ Thư Giãn Daybed",
                "Bed",
                "models/bed_day.glb",
                6500000f,
                null,
                "Giường đơn thư giãn Daybed phong cách Quiet Luxury tối giản, tích hợp đệm bọc vải lanh Linen tự nhiên thoáng mát vào mùa hè và ấm áp vào mùa đông. Khung gỗ tần bì tự nhiên phủ sơn gốc nước thân thiện môi trường, hoàn hảo cho góc nghỉ ngơi phòng đọc sách hay ban công ngập nắng.",
                Arrays.asList(woodBrown),
                Arrays.asList("1m2 x 2m", "1m4 x 2m"),
                Arrays.asList("file:///android_asset/product_images/bed_day.png")
        ));

        // ================= CUPBOARD =================
        list.add(new Product(
                "cupboard_01",
                "Tủ Gỗ Vintage Cổ Điển",
                "Cupboard",
                "models/cabinet_vintage.glb",
                5800000f,
                0.12f,
                "Tủ lưu trữ gỗ Vintage phong cách Mid-Century cổ điển với 6 ngăn kéo trượt êm ái trang bị ray giảm chấn Hafele tiêu chuẩn Đức. Toàn bộ thân tủ từ gỗ óc chó tự nhiên tuyển chọn, bề mặt đánh bóng thủ công bằng sáp ong bảo vệ thớ gỗ trường tồn cùng thời gian.",
                Arrays.asList(woodBrown, darkGray),
                Arrays.asList("1m2 x 90cm", "1m5 x 90cm"),
                Arrays.asList("file:///android_asset/product_images/cabinet_vintage.png")
        ));

        list.add(new Product(
                "cupboard_02",
                "Kệ Trưng Bày Đa Tầng Shelves",
                "Cupboard",
                "models/cupboard_shelves.glb",
                3200000f,
                null,
                "Kệ trưng bày đa tầng Shelves thiết kế bất đối xứng hiện đại, kết hợp hài hòa giữa các đợt gỗ sồi tự nhiên và khung thép sơn tĩnh điện chống xước. Tối ưu diện tích trưng bày sách quý, đồ gốm nghệ thuật và các bộ sưu tập yêu thích của gia chủ.",
                Arrays.asList(darkGray, woodBrown),
                Arrays.asList("1m2 x 1m8"),
                Arrays.asList("file:///android_asset/product_images/cupboard_shelves.png")
        ));

        return list;
    }
}
