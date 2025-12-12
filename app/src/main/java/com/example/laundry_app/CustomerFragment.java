package com.example.laundry_app; // Atau ganti sesuai package Anda

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

public class CustomerFragment extends Fragment {

    private TextInputLayout tilName, tilAddress, tilPhone;
    private EditText etName, etAddress, etPhone;
    private Button btnSave, btnCancel;
    private RecyclerView recyclerView;
    private DatabaseHelper dbHelper;
    private CustomerAdapter customerAdapter;
    private List<Customer> customerList = new ArrayList<>();
    private long customerIdToUpdate = -1; // -1 = add mode

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer, container, false);

        dbHelper = new DatabaseHelper(getContext());

        tilName = view.findViewById(R.id.til_customer_name);
        tilAddress = view.findViewById(R.id.til_customer_address);
        tilPhone = view.findViewById(R.id.til_customer_phone);
        etName = view.findViewById(R.id.et_customer_name);
        etAddress = view.findViewById(R.id.et_customer_address);
        etPhone = view.findViewById(R.id.et_customer_phone);
        btnSave = view.findViewById(R.id.btn_save_customer);
        btnCancel = view.findViewById(R.id.btn_cancel_update);
        recyclerView = view.findViewById(R.id.recycler_view_customers);

        setupRecyclerView();
        loadCustomersFromDB();

        btnSave.setOnClickListener(v -> saveOrUpdateCustomer());
        btnCancel.setOnClickListener(v -> switchToAddMode());

        switchToAddMode(); // default: Add mode
        return view;
    }

    private void setupRecyclerView() {
        customerAdapter = new CustomerAdapter(customerList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(customerAdapter);

        customerAdapter.setOnItemClickListener(new CustomerAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(int position) {
                switchToUpdateMode(customerList.get(position));
            }

            @Override
            public void onDeleteClick(int position) {
                showDeleteConfirmationDialog(customerList.get(position));
            }
        });
    }

    private void loadCustomersFromDB() {
        if (getContext() == null) return;
        List<Customer> newCustomers = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_CUSTOMERS, null);

        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CUSTOMER_NAME));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CUSTOMER_ADDRESS));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CUSTOMER_PHONE));
                newCustomers.add(new Customer(id, name, address, phone));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        customerAdapter.updateData(newCustomers);
        customerList.clear();
        customerList.addAll(newCustomers);
    }

    private void saveOrUpdateCustomer() {
        if (customerIdToUpdate == -1) {
            addCustomer();
        } else {
            updateCustomer();
        }
    }

    private void addCustomer() {
        if (getContext() == null) return;

        tilName.setError(null);

        String name = etName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty()) {
            tilName.setError("Nama pelanggan tidak boleh kosong");
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CUSTOMER_NAME, name);
        values.put(DatabaseHelper.COLUMN_CUSTOMER_ADDRESS, address);
        values.put(DatabaseHelper.COLUMN_CUSTOMER_PHONE, phone);

        long newRowId = db.insert(DatabaseHelper.TABLE_CUSTOMERS, null, values);

        if (newRowId != -1) {
            Toast.makeText(getContext(), "Pelanggan berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
            clearFields();
            loadCustomersFromDB();
        } else {
            Toast.makeText(getContext(), "Gagal menambahkan pelanggan.", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }

    private void updateCustomer() {
        String name = etName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty()) {
            tilName.setError("Nama pelanggan tidak boleh kosong");
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CUSTOMER_NAME, name);
        values.put(DatabaseHelper.COLUMN_CUSTOMER_ADDRESS, address);
        values.put(DatabaseHelper.COLUMN_CUSTOMER_PHONE, phone);

        int rowsAffected = db.update(DatabaseHelper.TABLE_CUSTOMERS, values, DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(customerIdToUpdate)});
        db.close();

        if (rowsAffected > 0) {
            Toast.makeText(getContext(), "Data pelanggan berhasil diperbarui.", Toast.LENGTH_SHORT).show();
            switchToAddMode();
            loadCustomersFromDB();
        } else {
            Toast.makeText(getContext(), "Gagal memperbarui data.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog(Customer customer) {
        new AlertDialog.Builder(getContext())
                .setTitle("Hapus Pelanggan")
                .setMessage("Apakah Anda yakin ingin menghapus " + customer.getName() + "?")
                .setPositiveButton("Hapus", (dialog, which) -> deleteCustomer(customer.getId()))
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deleteCustomer(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = db.delete(DatabaseHelper.TABLE_CUSTOMERS, DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();

        if (rowsAffected > 0) {
            Toast.makeText(getContext(), "Pelanggan berhasil dihapus.", Toast.LENGTH_SHORT).show();
            loadCustomersFromDB();
        } else {
            Toast.makeText(getContext(), "Gagal menghapus pelanggan.", Toast.LENGTH_SHORT).show();
        }
    }

    private void switchToUpdateMode(Customer customer) {
        customerIdToUpdate = customer.getId();
        etName.setText(customer.getName());
        etAddress.setText(customer.getAddress());
        etPhone.setText(customer.getPhone());
        btnSave.setText("Perbarui Pelanggan");
        btnCancel.setVisibility(View.VISIBLE);
    }

    private void switchToAddMode() {
        customerIdToUpdate = -1;
        clearFields();
        btnSave.setText("Tambah Pelanggan");
        btnCancel.setVisibility(View.GONE);
    }

    private void clearFields() {
        etName.setText("");
        etAddress.setText("");
        etPhone.setText("");
        tilName.setError(null);
    }
}
