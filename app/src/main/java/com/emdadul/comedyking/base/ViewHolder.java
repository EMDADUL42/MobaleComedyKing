package com.emdadul.comedyking.base;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

public class ViewHolder<T extends ViewBinding> extends RecyclerView.ViewHolder {

    public final T binding;

    public ViewHolder(T binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}
