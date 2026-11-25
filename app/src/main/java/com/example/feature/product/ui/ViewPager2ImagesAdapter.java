package com.example.feature.product.ui;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.example.aroom.databinding.ViewpagerImageItemBinding;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Objects;

public class ViewPager2ImagesAdapter extends RecyclerView.Adapter<ViewPager2ImagesAdapter.ViewPager2ImagesViewHolder> {

    @Nullable
    private Integer selectedColor = null;

    public void setSelectedColor(@Nullable Integer color) {
        if (!Objects.equals(this.selectedColor, color)) {
            this.selectedColor = color;
            notifyDataSetChanged();
        }
    }

    public class ViewPager2ImagesViewHolder extends RecyclerView.ViewHolder {
        private final ViewpagerImageItemBinding binding;

        public ViewPager2ImagesViewHolder(@NonNull ViewpagerImageItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String imagePath) {
            if (selectedColor == null) {
                Glide.with(itemView).load(imagePath).into(binding.imgProductDetails);
            } else {
                Glide.with(itemView)
                        .load(imagePath)
                        .transform(new FurnitureColorTransformation(selectedColor))
                        .into(binding.imgProductDetails);
            }
        }
    }

    private final DiffUtil.ItemCallback<String> diffCallback = new DiffUtil.ItemCallback<String>() {
        @Override
        public boolean areItemsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return oldItem.equals(newItem);
        }
    };

    public final AsyncListDiffer<String> differ = new AsyncListDiffer<>(this, diffCallback);

    @NonNull
    @Override
    public ViewPager2ImagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewpagerImageItemBinding binding = ViewpagerImageItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewPager2ImagesViewHolder(binding);
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewPager2ImagesViewHolder holder, int position) {
        String image = differ.getCurrentList().get(position);
        holder.bind(image);
    }

    private static class FurnitureColorTransformation extends BitmapTransformation {
        private static final String ID = "com.example.feature.product.ui.FurnitureColorTransformation.v1";
        private final int targetColor;

        FurnitureColorTransformation(int targetColor) {
            this.targetColor = targetColor;
        }

        @Override
        protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
            int width = toTransform.getWidth();
            int height = toTransform.getHeight();
            int total = width * height;
            if (total == 0) return toTransform;

            Bitmap src = toTransform.getConfig() == Bitmap.Config.ARGB_8888
                    ? toTransform
                    : toTransform.copy(Bitmap.Config.ARGB_8888, false);
            int[] pixels = new int[total];
            src.getPixels(pixels, 0, width, 0, 0, width, height);

            // Flood-fill outer studio white background from borders so interior whites stay intact
            boolean[] isBackground = new boolean[total];
            int[] queue = new int[total];
            int head = 0;
            int tail = 0;

            for (int x = 0; x < width; x++) {
                if (isNearWhite(pixels[x]) && !isBackground[x]) {
                    isBackground[x] = true;
                    queue[tail++] = x;
                }
                int bottom = (height - 1) * width + x;
                if (isNearWhite(pixels[bottom]) && !isBackground[bottom]) {
                    isBackground[bottom] = true;
                    queue[tail++] = bottom;
                }
            }
            for (int y = 0; y < height; y++) {
                int left = y * width;
                if (isNearWhite(pixels[left]) && !isBackground[left]) {
                    isBackground[left] = true;
                    queue[tail++] = left;
                }
                int right = y * width + (width - 1);
                if (isNearWhite(pixels[right]) && !isBackground[right]) {
                    isBackground[right] = true;
                    queue[tail++] = right;
                }
            }

            while (head < tail) {
                int idx = queue[head++];
                int x = idx % width;
                int y = idx / width;

                if (x > 0) {
                    int n = idx - 1;
                    if (!isBackground[n] && isNearWhite(pixels[n])) {
                        isBackground[n] = true;
                        queue[tail++] = n;
                    }
                }
                if (x + 1 < width) {
                    int n = idx + 1;
                    if (!isBackground[n] && isNearWhite(pixels[n])) {
                        isBackground[n] = true;
                        queue[tail++] = n;
                    }
                }
                if (y > 0) {
                    int n = idx - width;
                    if (!isBackground[n] && isNearWhite(pixels[n])) {
                        isBackground[n] = true;
                        queue[tail++] = n;
                    }
                }
                if (y + 1 < height) {
                    int n = idx + width;
                    if (!isBackground[n] && isNearWhite(pixels[n])) {
                        isBackground[n] = true;
                        queue[tail++] = n;
                    }
                }
            }

            // Compute mean luminance of foreground so dark woods and light fabrics both recolor naturally
            float lumSum = 0f;
            int fgCount = 0;
            for (int i = 0; i < total; i++) {
                if (isBackground[i]) continue;
                int p = pixels[i];
                if (((p >>> 24) & 0xFF) < 20) continue;
                int r = (p >> 16) & 0xFF;
                int g = (p >> 8) & 0xFF;
                int b = p & 0xFF;
                lumSum += (0.299f * r + 0.587f * g + 0.114f * b) / 255f;
                fgCount++;
            }
            float meanLum = fgCount > 0 ? Math.max(0.24f, Math.min(0.80f, lumSum / fgCount)) : 0.5f;

            float tr = Color.red(targetColor) / 255f;
            float tg = Color.green(targetColor) / 255f;
            float tb = Color.blue(targetColor) / 255f;
            // Slightly lift very dark colors (like darkGray/navyBlue) so surface details remain visible
            float targetLum = 0.299f * tr + 0.587f * tg + 0.114f * tb;
            if (targetLum < 0.25f) {
                float lift = 1.25f;
                tr = Math.min(1f, tr * lift + 0.04f);
                tg = Math.min(1f, tg * lift + 0.04f);
                tb = Math.min(1f, tb * lift + 0.04f);
            }

            for (int i = 0; i < total; i++) {
                if (isBackground[i]) continue;
                int p = pixels[i];
                int a = (p >>> 24) & 0xFF;
                if (a < 20) continue;

                int r = (p >> 16) & 0xFF;
                int g = (p >> 8) & 0xFF;
                int b = p & 0xFF;

                float lum = (0.299f * r + 0.587f * g + 0.114f * b) / 255f;
                float relShade = Math.max(0.22f, Math.min(1.55f, lum / meanLum));

                // Smooth fade near white anti-aliased edges
                int minC = Math.min(r, Math.min(g, b));
                int maxC = Math.max(r, Math.max(g, b));
                float tintWeight = 1.0f;
                if (minC >= 218 && (maxC - minC) <= 20) {
                    tintWeight = Math.max(0f, (242f - minC) / 24f);
                }

                float coloredR = Math.min(1f, tr * relShade);
                float coloredG = Math.min(1f, tg * relShade);
                float coloredB = Math.min(1f, tb * relShade);

                float outR = (r / 255f) * (1f - tintWeight) + coloredR * tintWeight;
                float outG = (g / 255f) * (1f - tintWeight) + coloredG * tintWeight;
                float outB = (b / 255f) * (1f - tintWeight) + coloredB * tintWeight;

                int nr = Math.max(0, Math.min(255, Math.round(outR * 255f)));
                int ng = Math.max(0, Math.min(255, Math.round(outG * 255f)));
                int nb = Math.max(0, Math.min(255, Math.round(outB * 255f)));
                pixels[i] = (a << 24) | (nr << 16) | (ng << 8) | nb;
            }

            Bitmap out = pool.get(width, height, Bitmap.Config.ARGB_8888);
            out.setPixels(pixels, 0, width, 0, 0, width, height);
            if (src != toTransform) {
                src.recycle();
            }
            return out;
        }

        private static boolean isNearWhite(int pixel) {
            int a = (pixel >>> 24) & 0xFF;
            if (a < 20) return true;
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            int min = Math.min(r, Math.min(g, b));
            int max = Math.max(r, Math.max(g, b));
            return min >= 238 && (max - min) <= 16;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FurnitureColorTransformation)) return false;
            return targetColor == ((FurnitureColorTransformation) o).targetColor;
        }

        @Override
        public int hashCode() {
            return Objects.hash(ID, targetColor);
        }

        @Override
        public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
            messageDigest.update((ID).getBytes(CHARSET));
            messageDigest.update(ByteBuffer.allocate(4).putInt(targetColor).array());
        }
    }
}
