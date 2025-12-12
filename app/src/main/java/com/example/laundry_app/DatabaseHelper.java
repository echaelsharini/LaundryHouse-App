package com.example.laundry_app;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DatabaseHelper class to manage the creation and versioning of the SQLite database.
 * This class defines the table structures and provides a way to get readable/writable
 * database instances.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // --- DATABASE INFORMATION ---
    private static final String DATABASE_NAME = "laundry.db";
    private static final int DATABASE_VERSION = 1;

    // --- TABLE NAMES ---
    public static final String TABLE_USERS = "users";
    public static final String TABLE_CUSTOMERS = "customers";
    public static final String TABLE_SERVICES = "services";
    public static final String TABLE_TRANSACTIONS = "transactions";

    // --- COMMON COLUMN NAMES ---
    public static final String COLUMN_ID = "_id";

    // --- USERS TABLE COLUMNS ---
    public static final String COLUMN_USER_USERNAME = "username";
    public static final String COLUMN_USER_PASSWORD = "password";

    // --- CUSTOMERS TABLE COLUMNS ---
    public static final String COLUMN_CUSTOMER_NAME = "name";
    public static final String COLUMN_CUSTOMER_ADDRESS = "address";
    public static final String COLUMN_CUSTOMER_PHONE = "phone";

    // --- SERVICES TABLE COLUMNS ---
    public static final String COLUMN_SERVICE_TYPE = "service_type";
    public static final String COLUMN_SERVICE_PRICE_PER_KG = "price_per_kg";
    public static final String COLUMN_SERVICE_ESTIMATED_DAYS = "estimated_days";

    // --- TRANSACTIONS TABLE COLUMNS ---
    public static final String COLUMN_TRANSACTION_CUSTOMER_ID = "customer_id";
    public static final String COLUMN_TRANSACTION_SERVICE_ID = "service_id";
    public static final String COLUMN_TRANSACTION_WEIGHT_KG = "weight_kg";
    public static final String COLUMN_TRANSACTION_TOTAL_PRICE = "total_price";
    public static final String COLUMN_TRANSACTION_DATE = "transaction_date";
    public static final String COLUMN_TRANSACTION_STATUS = "status"; // e.g., "In Progress", "Finished"

    // --- TABLE CREATION SQL STATEMENTS ---

    // Create Users table SQL
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_USERNAME + " TEXT NOT NULL UNIQUE,"
            + COLUMN_USER_PASSWORD + " TEXT NOT NULL"
            + ");";

    // Create Customers table SQL
    private static final String CREATE_TABLE_CUSTOMERS = "CREATE TABLE " + TABLE_CUSTOMERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_CUSTOMER_NAME + " TEXT NOT NULL,"
            + COLUMN_CUSTOMER_ADDRESS + " TEXT,"
            + COLUMN_CUSTOMER_PHONE + " TEXT"
            + ");";

    // Create Services table SQL
    private static final String CREATE_TABLE_SERVICES = "CREATE TABLE " + TABLE_SERVICES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_SERVICE_TYPE + " TEXT NOT NULL,"
            + COLUMN_SERVICE_PRICE_PER_KG + " REAL NOT NULL,"
            + COLUMN_SERVICE_ESTIMATED_DAYS + " INTEGER NOT NULL"
            + ");";

    // Create Transactions table SQL
    private static final String CREATE_TABLE_TRANSACTIONS = "CREATE TABLE " + TABLE_TRANSACTIONS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_TRANSACTION_CUSTOMER_ID + " INTEGER NOT NULL,"
            + COLUMN_TRANSACTION_SERVICE_ID + " INTEGER NOT NULL,"
            + COLUMN_TRANSACTION_WEIGHT_KG + " REAL NOT NULL,"
            + COLUMN_TRANSACTION_TOTAL_PRICE + " REAL NOT NULL,"
            + COLUMN_TRANSACTION_DATE + " TEXT NOT NULL,"
            + COLUMN_TRANSACTION_STATUS + " TEXT NOT NULL,"
            + "FOREIGN KEY(" + COLUMN_TRANSACTION_CUSTOMER_ID + ") REFERENCES " + TABLE_CUSTOMERS + "(" + COLUMN_ID + "),"
            + "FOREIGN KEY(" + COLUMN_TRANSACTION_SERVICE_ID + ") REFERENCES " + TABLE_SERVICES + "(" + COLUMN_ID + ")"
            + ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time.
     * This is where the creation of tables and the initial population of the tables should happen.
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Execute the SQL statements to create the tables
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_CUSTOMERS);
        db.execSQL(CREATE_TABLE_SERVICES);
        db.execSQL(CREATE_TABLE_TRANSACTIONS);

        // --- OPTIONAL: Insert a default user for initial login ---
        addDefaultUser(db);
    }

    /**
     * Called when the database needs to be upgraded.
     * This method will drop all old tables and create them again.
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVICES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Create tables again
        onCreate(db);
    }

    /**
     * Adds a default user to the database for testing purposes.
     * @param db The SQLiteDatabase instance.
     */
    private void addDefaultUser(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_USERNAME, "admin");
        values.put(COLUMN_USER_PASSWORD, "password123");
        db.insert(TABLE_USERS, null, values);
    }
}
