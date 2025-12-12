package com.example.laundry_app;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.List;

public class ServiceFragment extends Fragment {

    private TextInputLayout tilType, tilPrice, tilDays;
    private EditText etType, etPrice, etDays;
    private Button btnSave, btnCancel;
    private RecyclerView recyclerView;
    private DatabaseHelper dbHelper;
    private ServiceAdapter serviceAdapter;
    private List<Service> serviceList = new ArrayList<>();
    private long serviceIdToUpdate = -1; // -1 untuk mode tambah, >0 untuk mode update

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service, container, false);

        dbHelper = new DatabaseHelper(getContext());
        tilType = view.findViewById(R.id.til_service_type);
        tilPrice = view.findViewById(R.id.til_service_price);
        tilDays = view.findViewById(R.id.til_service_days);
        etType = view.findViewById(R.id.et_service_type);
        etPrice = view.findViewById(R.id.et_service_price);
        etDays = view.findViewById(R.id.et_service_days);
        btnSave = view.findViewById(R.id.btn_save_service);
        btnCancel = view.findViewById(R.id.btn_cancel_update_service);
        recyclerView = view.findViewById(R.id.recycler_view_services);

        setupRecyclerView();
        loadServicesFromDB();

        btnSave.setOnClickListener(v -> saveOrUpdateService());
        btnCancel.setOnClickListener(v -> switchToAddMode());

        return view;
    }

    private void setupRecyclerView() {
        serviceAdapter = new ServiceAdapter(serviceList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(serviceAdapter);

        serviceAdapter.setOnItemClickListener(new ServiceAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(int position) {
                switchToUpdateMode(serviceList.get(position));
            };

            @Override
            public void onDeleteClick(int position) {
                showDeleteConfirmationDialog(serviceList.get(position));
            }
        });
    }

    private void saveOrUpdateService() {
        if (serviceIdToUpdate == -1) {
            addService();
        } else {
            updateService();
        }
    }

    private void switchToUpdateMode(Service service) {
        serviceIdToUpdate = service.getId();
        etType.setText(service.getType());
        etPrice.setText(String.valueOf(service.getPricePerKg()));
        etDays.setText(String.valueOf(service.getEstimatedDays()));
        btnSave.setText("Perbarui Layanan");
        btnCancel.setVisibility(View.VISIBLE);
    }

    private void switchToAddMode() {
        serviceIdToUpdate = -1;
        clearFields();
        btnSave.setText("Tambah Layanan");
        btnCancel.setVisibility(View.GONE);
    }

    private void showDeleteConfirmationDialog(Service service) {
        new AlertDialog.Builder(getContext())
                .setTitle("Hapus Layanan")
                .setMessage("Yakin ingin menghapus layanan '" + service.getType() + "'?")
                .setPositiveButton("Hapus", (dialog, which) -> deleteService(service.getId()))
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deleteService(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = db.delete(DatabaseHelper.TABLE_SERVICES, DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        if (rowsAffected > 0) {
            Toast.makeText(getContext(), "Layanan berhasil dihapus.", Toast.LENGTH_SHORT).show();
            loadServicesFromDB();
        } else {
            Toast.makeText(getContext(), "Gagal menghapus layanan.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateService() {
        if (!validateInput()) return;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_SERVICE_TYPE, etType.getText().toString().trim());
        values.put(DatabaseHelper.COLUMN_SERVICE_PRICE_PER_KG, Double.parseDouble(etPrice.getText().toString()));
        values.put(DatabaseHelper.COLUMN_SERVICE_ESTIMATED_DAYS, Integer.parseInt(etDays.getText().toString()));

        int rowsAffected = db.update(DatabaseHelper.TABLE_SERVICES, values, DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(serviceIdToUpdate)});
        db.close();

        if (rowsAffected > 0) {
            Toast.makeText(getContext(), "Layanan berhasil diperbarui.", Toast.LENGTH_SHORT).show();
            switchToAddMode();
            loadServicesFromDB();
        } else {
            Toast.makeText(getContext(), "Gagal memperbarui layanan.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addService() {
        if (!validateInput()) return;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_SERVICE_TYPE, etType.getText().toString().trim());
        values.put(DatabaseHelper.COLUMN_SERVICE_PRICE_PER_KG, Double.parseDouble(etPrice.getText().toString()));
        values.put(DatabaseHelper.COLUMN_SERVICE_ESTIMATED_DAYS, Integer.parseInt(etDays.getText().toString()));

        long newRowId = db.insert(DatabaseHelper.TABLE_SERVICES, null, values);
        if (newRowId != -1) {
            Toast.makeText(getContext(), "Layanan berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
            clearFields();
            loadServicesFromDB();
        } else {
            Toast.makeText(getContext(), "Gagal menambahkan layanan.", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }

    private void loadServicesFromDB() {
        if (getContext() == null) return;
        List<Service> newServices = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_SERVICES, null);
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SERVICE_TYPE));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SERVICE_PRICE_PER_KG));
                int days = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SERVICE_ESTIMATED_DAYS));
                newServices.add(new Service(id, type, price, days));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        serviceAdapter.updateData(newServices);
    }

    private boolean validateInput() {
        tilType.setError(null);
        tilPrice.setError(null);
        tilDays.setError(null);

        if (etType.getText().toString().trim().isEmpty()) {
            tilType.setError("Tipe layanan tidak boleh kosong");
            return false;
        }
        if (etPrice.getText().toString().trim().isEmpty()) {
            tilPrice.setError("Harga tidak boleh kosong");
            return false;
        }
        if (etDays.getText().toString().trim().isEmpty()) {
            tilDays.setError("Estimasi hari tidak boleh kosong");
            return false;
        }
        return true;
    }

    private void clearFields() {
        etType.setText("");
        etPrice.setText("");
        etDays.setText("");
        tilType.setError(null);
        tilPrice.setError(null);
        tilDays.setError(null);
    }
}