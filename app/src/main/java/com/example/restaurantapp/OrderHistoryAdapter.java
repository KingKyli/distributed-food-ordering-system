package com.example.restaurantapp;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.VH> {

    private final List<OrderRecord> orders;

    public OrderHistoryAdapter(List<OrderRecord> orders) {
        this.orders = orders;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_history, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        OrderRecord record = orders.get(position);
        holder.tvStoreName.setText(record.getStoreName());
        holder.tvDate.setText(record.getFormattedDate());
        holder.tvTotal.setText(String.format(Locale.getDefault(), "\u20AC%.2f", record.getTotal()));

        // Build items summary string
        List<String> summaries = record.getItemSummaries();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < summaries.size(); i++) {
            if (i > 0) sb.append("  \u00B7  ");
            sb.append(summaries.get(i));
        }
        holder.tvItems.setText(sb.length() > 0 ? sb.toString() : "No items");

        // Reorder → open MainActivity with store name prefilled
        holder.btnReorder.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), MainActivity.class);
            intent.putExtra("SEARCH_QUERY", record.getStoreName());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return orders.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvStoreName, tvDate, tvTotal, tvItems;
        com.google.android.material.button.MaterialButton btnReorder;

        VH(@NonNull View itemView) {
            super(itemView);
            tvStoreName = itemView.findViewById(R.id.tvOrderStoreName);
            tvDate      = itemView.findViewById(R.id.tvOrderDate);
            tvTotal     = itemView.findViewById(R.id.tvOrderTotal);
            tvItems     = itemView.findViewById(R.id.tvOrderItems);
            btnReorder  = itemView.findViewById(R.id.btnReorder);
        }
    }
}

