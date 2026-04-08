package com.example.restaurantapp;

import android.content.Intent;
import android.graphics.Color;
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

        // Row 1 – Restaurant name
        holder.tvStoreName.setText(record.getStoreName());

        // Row 1 – Status chip
        bindStatusChip(holder.tvStatus, record.getStatus());

        // Row 2 – Date + Order ID
        String meta = record.getFormattedDate() + "  •  #" + record.getOrderId();
        holder.tvDate.setText(meta);

        // Row 3 – Items summary  (comma-separated)
        List<String> summaries = record.getItemSummaries();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < summaries.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(summaries.get(i));
        }
        holder.tvItems.setText(sb.length() > 0 ? sb.toString() : "No items");

        // Row 4 – Total
        holder.tvTotal.setText(String.format(Locale.getDefault(), "\u20AC%.2f", record.getTotal()));

        // Row 4 – Reorder → open MainActivity with store name prefilled
        holder.btnReorder.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), MainActivity.class);
            intent.putExtra("SEARCH_QUERY", record.getStoreName());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            v.getContext().startActivity(intent);
        });
    }

    private void bindStatusChip(TextView chip, OrderRecord.OrderStatus status) {
        if (status == null) status = OrderRecord.OrderStatus.DELIVERED;
        switch (status) {
            case PENDING:
                chip.setBackgroundResource(R.drawable.status_chip_pending);
                chip.setTextColor(Color.parseColor("#92400E"));
                chip.setText("Pending");
                break;
            case CANCELLED:
                chip.setBackgroundResource(R.drawable.status_chip_cancelled);
                chip.setTextColor(Color.parseColor("#991B1B"));
                chip.setText("Cancelled");
                break;
            case DELIVERED:
            default:
                chip.setBackgroundResource(R.drawable.status_chip_delivered);
                chip.setTextColor(Color.parseColor("#166534"));
                chip.setText("Delivered");
                break;
        }
    }

    @Override
    public int getItemCount() { return orders.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvStoreName, tvStatus, tvDate, tvItems, tvTotal, btnReorder;

        VH(@NonNull View itemView) {
            super(itemView);
            tvStoreName = itemView.findViewById(R.id.tvOrderStoreName);
            tvStatus    = itemView.findViewById(R.id.tvOrderStatus);
            tvDate      = itemView.findViewById(R.id.tvOrderDate);
            tvItems     = itemView.findViewById(R.id.tvOrderItems);
            tvTotal     = itemView.findViewById(R.id.tvOrderTotal);
            btnReorder  = itemView.findViewById(R.id.btnReorder);
        }
    }
}
