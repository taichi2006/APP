package com.example.feature.order.ui;

import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aroom.R;
import com.example.aroom.databinding.AddressRvItemBinding;
import com.example.model.Address;

import java.util.Objects;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    public interface OnAddressClickListener {
        void onAddressClick(Address address);
    }

    public int selectedAddress = -1;
    public OnAddressClickListener onClick = null;

    public void setOnClick(OnAddressClickListener listener) {
        this.onClick = listener;
    }

    public class AddressViewHolder extends RecyclerView.ViewHolder {
        public final AddressRvItemBinding binding;

        public AddressViewHolder(@NonNull AddressRvItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Address address, boolean isSelected) {
            binding.buttonAddress.setText(address.getAddressTitle());
            if (isSelected) {
                binding.buttonAddress.setBackground(new ColorDrawable(
                    ContextCompat.getColor(itemView.getContext(), R.color.g_blue)
                ));
            } else {
                binding.buttonAddress.setBackground(new ColorDrawable(
                    ContextCompat.getColor(itemView.getContext(), R.color.g_white)
                ));
            }
        }
    }

    private final DiffUtil.ItemCallback<Address> diffCallback = new DiffUtil.ItemCallback<Address>() {
        @Override
        public boolean areItemsTheSame(@NonNull Address oldItem, @NonNull Address newItem) {
            return Objects.equals(oldItem.getAddressTitle(), newItem.getAddressTitle())
                && Objects.equals(oldItem.getFullName(), newItem.getFullName());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Address oldItem, @NonNull Address newItem) {
            return Objects.equals(oldItem, newItem);
        }
    };

    public final AsyncListDiffer<Address> differ = new AsyncListDiffer<>(this, diffCallback);

    public AddressAdapter() {
        differ.addListListener((previousList, currentList) -> {
            if (selectedAddress >= 0) {
                notifyItemChanged(selectedAddress);
            }
        });
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AddressRvItemBinding binding = AddressRvItemBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false
        );
        return new AddressViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = differ.getCurrentList().get(position);
        holder.bind(address, selectedAddress == position);

        holder.binding.buttonAddress.setOnClickListener(v -> {
            if (selectedAddress >= 0) {
                notifyItemChanged(selectedAddress);
            }
            selectedAddress = holder.getAdapterPosition();
            notifyItemChanged(selectedAddress);
            if (onClick != null) {
                onClick.onAddressClick(address);
            }
        });
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }
}
