package com.example.laundry_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<Service> serviceList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ServiceAdapter(List<Service> serviceList) {
        this.serviceList = serviceList;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.service_item, parent, false);
        return new ServiceViewHolder(itemView, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service currentService = serviceList.get(position);
        holder.tvServiceType.setText(currentService.getType());
        holder.tvServicePrice.setText(String.format(Locale.getDefault(), "Harga: Rp %,.0f /kg", currentService.getPricePerKg()));
        holder.tvServiceDays.setText(String.format(Locale.getDefault(), "Estimasi: %d hari", currentService.getEstimatedDays()));
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public void updateData(List<Service> newServiceList) {
        serviceList.clear();
        serviceList.addAll(newServiceList);
        notifyDataSetChanged();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceType, tvServicePrice, tvServiceDays;
        ImageButton btnEdit, btnDelete;

        public ServiceViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            tvServiceType = itemView.findViewById(R.id.tv_service_type);
            tvServicePrice = itemView.findViewById(R.id.tv_service_price);
            tvServiceDays = itemView.findViewById(R.id.tv_service_days);
            btnEdit = itemView.findViewById(R.id.btn_edit_service);
            btnDelete = itemView.findViewById(R.id.btn_delete_service);

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onEditClick(position);
                    }
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(position);
                    }
                }
            });
        }
    }
}