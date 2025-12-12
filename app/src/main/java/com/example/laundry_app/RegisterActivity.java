package com.example.laundry_app;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etConfirmPassword;
    private Button btnRegister;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        etUsername = findViewById(R.id.editTextUsernameRegister);
        etPassword = findViewById(R.id.editTextPasswordRegister);
        etConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        btnRegister = findViewById(R.id.buttonRegister);

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Harap isi semua kolom", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Password tidak cocok", Toast.LENGTH_SHORT).show();
            return;
        }

        // ===========================
        // BACKGROUND TASK DIMULAI
        // ===========================
        new Thread(() -> {

            // 1. Cek apakah username sudah ada (dilakukan di background)
            if (checkUserExists(username)) {
                runOnUiThread(() ->
                        Toast.makeText(RegisterActivity.this,
                                "Username sudah digunakan, silakan pilih yang lain.",
                                Toast.LENGTH_LONG).show()
                );
                return; // Stop background thread
            }

            // 2. Insert user baru
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_USER_USERNAME, username);
            values.put(DatabaseHelper.COLUMN_USER_PASSWORD, password);

            long newRowId = db.insert(DatabaseHelper.TABLE_USERS, null, values);
            db.close();

            // 3. Kembali ke UI thread untuk update tampilan
            runOnUiThread(() -> {
                if (newRowId == -1) {
                    Toast.makeText(RegisterActivity.this,
                            "Gagal mendaftar.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(RegisterActivity.this,
                            "Registrasi berhasil!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

        }).start();
        // ===========================
        // BACKGROUND TASK SELESAI
        // ===========================
    }

    /**
     * Mengecek apakah username sudah ada di database.
     * (Dipanggil dari background thread)
     */
    private boolean checkUserExists(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = { DatabaseHelper.COLUMN_ID };
        String selection = DatabaseHelper.COLUMN_USER_USERNAME + " = ?";
        String[] selectionArgs = { username };

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count > 0;
    }
}
