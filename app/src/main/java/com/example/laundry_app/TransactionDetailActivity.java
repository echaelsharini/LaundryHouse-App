package com.example.laundry_app;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionDetailActivity extends AppCompatActivity {

    private TextView tvCustomer, tvDate, tvService, tvWeight, tvTotal;
    private Spinner spinnerStatus;
    private Button btnSaveStatus, btnDelete;

    private DatabaseHelper dbHelper;
    private long transactionId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

        dbHelper = new DatabaseHelper(this);

        // Ambil ID dari Intent
        transactionId = getIntent().getLongExtra("transaction_id", -1);

        // Binding Views
        tvCustomer = findViewById(R.id.tv_detail_customer);
        tvDate = findViewById(R.id.tv_detail_date);
        tvService = findViewById(R.id.tv_detail_service);
        tvWeight = findViewById(R.id.tv_detail_weight);
        tvTotal = findViewById(R.id.tv_detail_total);
        spinnerStatus = findViewById(R.id.spinner_detail_status);
        btnSaveStatus = findViewById(R.id.btn_save_status);
        btnDelete = findViewById(R.id.btn_delete_transaction);

        // Setup Spinner Status
        setupStatusSpinner();

        // Load Data Transaksi
        if (transactionId != -1) {
            loadTransactionDetails(transactionId);
        } else {
            Toast.makeText(this, "Data transaksi tidak valid", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Listener Tombol Simpan Status
        btnSaveStatus.setOnClickListener(v -> updateStatus());

        // Listener Tombol Hapus
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void setupStatusSpinner() {
        List<String> statuses = new ArrayList<>();
        statuses.add("In Progress");
        statuses.add("Finished");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);
    }

    private void loadTransactionDetails(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query Join untuk mengambil detail lengkap
        String query = "SELECT T.*, C." + DatabaseHelper.COLUMN_CUSTOMER_NAME + ", S." + DatabaseHelper.COLUMN_SERVICE_TYPE +
                " FROM " + DatabaseHelper.TABLE_TRANSACTIONS + " T " +
                " JOIN " + DatabaseHelper.TABLE_CUSTOMERS + " C ON T." + DatabaseHelper.COLUMN_TRANSACTION_CUSTOMER_ID + " = C." + DatabaseHelper.COLUMN_ID +
                " JOIN " + DatabaseHelper.TABLE_SERVICES + " S ON T." + DatabaseHelper.COLUMN_TRANSACTION_SERVICE_ID + " = S." + DatabaseHelper.COLUMN_ID +
                " WHERE T." + DatabaseHelper.COLUMN_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});

        if (cursor.moveToFirst()) {
            String customerName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CUSTOMER_NAME));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_DATE));
            String serviceType = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SERVICE_TYPE));
            double weight = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_WEIGHT_KG));
            double totalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_TOTAL_PRICE));
            String currentStatus = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_STATUS));

            // Set Text
            tvCustomer.setText(customerName);
            tvDate.setText(date);
            tvService.setText(serviceType);
            tvWeight.setText(String.format(Locale.getDefault(), "%.1f kg", weight));
            tvTotal.setText(String.format(Locale.getDefault(), "Rp %,.0f", totalPrice));

            // Set Spinner Selection berdasarkan status database
            if (currentStatus.equalsIgnoreCase("Finished")) {
                spinnerStatus.setSelection(1);
            } else {
                spinnerStatus.setSelection(0);
            }
        }
        cursor.close();
        db.close();
    }

    private void updateStatus() {
        String newStatus = spinnerStatus.getSelectedItem().toString();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TRANSACTION_STATUS, newStatus);

        int rows = db.update(DatabaseHelper.TABLE_TRANSACTIONS, values,
                DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(transactionId)});
        db.close();

        if (rows > 0) {
            Toast.makeText(this, "Status berhasil diperbarui!", Toast.LENGTH_SHORT).show();
            finish(); // <--- INI AKAN MENUTUP HALAMAN DAN KEMBALI KE LIST
        } else {
            Toast.makeText(this, "Gagal memperbarui status.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Transaksi")
                .setMessage("Apakah Anda yakin ingin menghapus data transaksi ini? Tindakan ini tidak dapat dibatalkan.")
                .setPositiveButton("Hapus", (dialog, which) -> deleteTransaction())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deleteTransaction() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_TRANSACTIONS,
                DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(transactionId)});
        db.close();

        if (rows > 0) {
            Toast.makeText(this, "Transaksi berhasil dihapus", Toast.LENGTH_SHORT).show();
            finish(); // <--- INI SUDAH ADA, MENUTUP HALAMAN SETELAH DELETE
        } else {
            Toast.makeText(this, "Gagal menghapus transaksi", Toast.LENGTH_SHORT).show();
        }
    }
}