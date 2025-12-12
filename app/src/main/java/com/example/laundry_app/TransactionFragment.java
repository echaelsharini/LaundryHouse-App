package com.example.laundry_app;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionFragment extends Fragment {

    private Spinner spinnerCustomer, spinnerService;
    private EditText etWeight;
    private TextView tvTotalPrice;
    private Button btnAddTransaction;
    private RecyclerView recyclerViewTransactions;

    private DatabaseHelper dbHelper;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList = new ArrayList<>();

    private List<String> customerNames = new ArrayList<>();
    private List<Integer> customerIds = new ArrayList<>();
    private List<String> serviceTypes = new ArrayList<>();
    private List<Double> servicePrices = new ArrayList<>();
    private List<Integer> serviceIds = new ArrayList<>();

    // Background Executor
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);

        dbHelper = new DatabaseHelper(getContext());

        spinnerCustomer = view.findViewById(R.id.spinner_customer);
        spinnerService = view.findViewById(R.id.spinner_service);
        etWeight = view.findViewById(R.id.et_weight);
        tvTotalPrice = view.findViewById(R.id.tv_total_price);
        btnAddTransaction = view.findViewById(R.id.btn_add_transaction);
        recyclerViewTransactions = view.findViewById(R.id.recycler_view_transactions);

        setupRecyclerView();
        loadSpinnerData();
        setupListeners();
        loadTransactionsFromDB();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSpinnerData();
        loadTransactionsFromDB();
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(transactionList, getContext());
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewTransactions.setAdapter(transactionAdapter);

        transactionAdapter.setOnStatusChangeListener((transactionId, newStatus) ->
                updateTransactionStatus(transactionId, newStatus)
        );

        transactionAdapter.setOnItemClickListener(transactionId -> {
            Intent intent = new Intent(getContext(), TransactionDetailActivity.class);
            intent.putExtra("transaction_id", transactionId);
            startActivity(intent);
        });
    }

    // ------------------ BACKGROUND TASK AREA ----------------------

    private void loadTransactionsFromDB() {
        executor.execute(() -> {
            List<Transaction> newTransactions = new ArrayList<>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            String query = "SELECT T." + DatabaseHelper.COLUMN_ID + ", " +
                    "C." + DatabaseHelper.COLUMN_CUSTOMER_NAME + ", " +
                    "S." + DatabaseHelper.COLUMN_SERVICE_TYPE + ", " +
                    "T." + DatabaseHelper.COLUMN_TRANSACTION_WEIGHT_KG + ", " +
                    "T." + DatabaseHelper.COLUMN_TRANSACTION_TOTAL_PRICE + ", " +
                    "T." + DatabaseHelper.COLUMN_TRANSACTION_DATE + ", " +
                    "T." + DatabaseHelper.COLUMN_TRANSACTION_STATUS + " " +
                    "FROM " + DatabaseHelper.TABLE_TRANSACTIONS + " T " +
                    "JOIN " + DatabaseHelper.TABLE_CUSTOMERS + " C ON T." + DatabaseHelper.COLUMN_TRANSACTION_CUSTOMER_ID + " = C." + DatabaseHelper.COLUMN_ID + " " +
                    "JOIN " + DatabaseHelper.TABLE_SERVICES + " S ON T." + DatabaseHelper.COLUMN_TRANSACTION_SERVICE_ID + " = S." + DatabaseHelper.COLUMN_ID +
                    " ORDER BY T." + DatabaseHelper.COLUMN_ID + " DESC";

            Cursor cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(0);
                    String customerName = cursor.getString(1);
                    String serviceType = cursor.getString(2);
                    double weight = cursor.getDouble(3);
                    double totalPrice = cursor.getDouble(4);
                    String date = cursor.getString(5);
                    String status = cursor.getString(6);

                    newTransactions.add(new Transaction(id, customerName, serviceType, weight, totalPrice, date, status));
                } while (cursor.moveToNext());
            }

            cursor.close();
            db.close();

            requireActivity().runOnUiThread(() ->
                    transactionAdapter.updateData(newTransactions)
            );
        });
    }

    private void loadSpinnerData() {
        loadCustomerData();
        loadServiceData();
    }

    private void loadCustomerData() {
        executor.execute(() -> {
            customerNames.clear();
            customerIds.clear();

            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_CUSTOMERS, null);

            if (cursor.moveToFirst()) {
                do {
                    customerIds.add(cursor.getInt(0));
                    customerNames.add(cursor.getString(1));
                } while (cursor.moveToNext());
            }

            cursor.close();
            db.close();

            requireActivity().runOnUiThread(() -> {
                ArrayAdapter<String> customerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, customerNames);
                customerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCustomer.setAdapter(customerAdapter);
            });
        });
    }

    private void loadServiceData() {
        executor.execute(() -> {
            serviceTypes.clear();
            servicePrices.clear();
            serviceIds.clear();

            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_SERVICES, null);

            if (cursor.moveToFirst()) {
                do {
                    serviceIds.add(cursor.getInt(0));
                    serviceTypes.add(cursor.getString(1));
                    servicePrices.add(cursor.getDouble(2));
                } while (cursor.moveToNext());
            }

            cursor.close();
            db.close();

            requireActivity().runOnUiThread(() -> {
                ArrayAdapter<String> serviceAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, serviceTypes);
                serviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerService.setAdapter(serviceAdapter);
            });
        });
    }

    private void updateTransactionStatus(long transactionId, String newStatus) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_TRANSACTION_STATUS, newStatus);

            int rows = db.update(DatabaseHelper.TABLE_TRANSACTIONS, values,
                    "id=?", new String[]{String.valueOf(transactionId)});
            db.close();

            requireActivity().runOnUiThread(() -> {
                if (rows > 0) {
                    Toast.makeText(getContext(), "Status diperbarui", Toast.LENGTH_SHORT).show();
                    loadTransactionsFromDB();
                }
            });
        });
    }

    private void addTransaction() {
        int customerPosition = spinnerCustomer.getSelectedItemPosition();
        int servicePosition = spinnerService.getSelectedItemPosition();
        String weightStr = etWeight.getText().toString();

        if (customerPosition < 0 || servicePosition < 0 || weightStr.isEmpty()) {
            Toast.makeText(getContext(), "Harap lengkapi semua data", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            int customerId = customerIds.get(customerPosition);
            int serviceId = serviceIds.get(servicePosition);
            double weight = Double.parseDouble(weightStr);
            double totalPrice = servicePrices.get(servicePosition) * weight;
            String currentDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
            String status = "In Progress";

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_TRANSACTION_CUSTOMER_ID, customerId);
            values.put(DatabaseHelper.COLUMN_TRANSACTION_SERVICE_ID, serviceId);
            values.put(DatabaseHelper.COLUMN_TRANSACTION_WEIGHT_KG, weight);
            values.put(DatabaseHelper.COLUMN_TRANSACTION_TOTAL_PRICE, totalPrice);
            values.put(DatabaseHelper.COLUMN_TRANSACTION_DATE, currentDate);
            values.put(DatabaseHelper.COLUMN_TRANSACTION_STATUS, status);

            long newRowId = db.insert(DatabaseHelper.TABLE_TRANSACTIONS, null, values);
            db.close();

            requireActivity().runOnUiThread(() -> {
                if (newRowId != -1) {
                    Toast.makeText(getContext(), "Transaksi berhasil!", Toast.LENGTH_SHORT).show();
                    etWeight.setText("");
                    tvTotalPrice.setText("Total Harga: Rp 0");
                    loadTransactionsFromDB();
                } else {
                    Toast.makeText(getContext(), "Gagal menambahkan transaksi.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // ---------------- END BACKGROUND TASK AREA ---------------------

    private void setupListeners() {
        spinnerService.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calculateTotalPrice();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        etWeight.setOnKeyListener((v, keyCode, event) -> {
            calculateTotalPrice();
            return false;
        });

        btnAddTransaction.setOnClickListener(v -> addTransaction());
    }

    private void calculateTotalPrice() {
        int servicePosition = spinnerService.getSelectedItemPosition();
        String weightStr = etWeight.getText().toString();

        if (servicePosition >= 0 && !weightStr.isEmpty()) {
            try {
                double pricePerKg = servicePrices.get(servicePosition);
                double weight = Double.parseDouble(weightStr);
                double totalPrice = pricePerKg * weight;
                tvTotalPrice.setText(String.format(Locale.getDefault(), "Total Harga: Rp %,.0f", totalPrice));
            } catch (Exception e) {
                tvTotalPrice.setText("Total Harga: Rp 0");
            }
        } else {
            tvTotalPrice.setText("Total Harga: Rp 0");
        }
    }
}
