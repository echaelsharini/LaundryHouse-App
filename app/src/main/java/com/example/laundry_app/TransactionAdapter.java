package com.example.laundry_app;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;
    private Context context;
    private OnStatusChangeListener statusListener;
    private OnItemClickListener itemClickListener; // Tambahkan listener klik item

    // Interface untuk Status (sudah ada sebelumnya)
    public interface OnStatusChangeListener {
        void onStatusChanged(long transactionId, String newStatus);
    }

    // Interface baru untuk Klik Item (Detail)
    public interface OnItemClickListener {
        void onItemClick(long transactionId);
    }

    public void setOnStatusChangeListener(OnStatusChangeListener listener) {
        this.statusListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public TransactionAdapter(List<Transaction> transactionList, Context context) {
        this.transactionList = transactionList;
        this.context = context;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transaction_item, parent, false);
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction currentTransaction = transactionList.get(position);

        holder.tvCustomerName.setText(currentTransaction.getCustomerName());
        holder.tvServiceType.setText(currentTransaction.getServiceType());
        holder.tvDate.setText(currentTransaction.getTransactionDate());

        String details = String.format(Locale.getDefault(), "%.1f kg • Rp %,.0f",
                currentTransaction.getWeight(), currentTransaction.getTotalPrice());
        holder.tvDetails.setText(details);

        // --- UI LOGIC STATUS ---
        String status = currentTransaction.getStatus();
        holder.tvStatus.setText(status);

        if ("Finished".equalsIgnoreCase(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_finished);
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_inprogress);
            holder.tvStatus.setTextColor(Color.parseColor("#EF6C00"));
        }

        // Klik Item Utama (Pindah ke Detail)
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(currentTransaction.getId());
            }
        });

        // Klik Status (Popup Menu Cepat)
        holder.tvStatus.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.tvStatus);
            popup.getMenu().add("In Progress");
            popup.getMenu().add("Finished");

            popup.setOnMenuItemClickListener(item -> {
                String selectedStatus = item.getTitle().toString();
                if (!selectedStatus.equals(currentTransaction.getStatus())) {
                    if (statusListener != null) {
                        statusListener.onStatusChanged(currentTransaction.getId(), selectedStatus);
                    }
                }
                return true;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public void updateData(List<Transaction> newTransactionList) {
        this.transactionList.clear();
        this.transactionList.addAll(newTransactionList);
        notifyDataSetChanged();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerName, tvServiceType, tvDetails, tvDate, tvStatus;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tv_trans_customer_name);
            tvServiceType = itemView.findViewById(R.id.tv_trans_service_type);
            tvDetails = itemView.findViewById(R.id.tv_trans_details);
            tvDate = itemView.findViewById(R.id.tv_trans_date);
            tvStatus = itemView.findViewById(R.id.tv_trans_status);
        }
    }
}