package it.hueic.kenhoang.orderfoods_app.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.List;

import it.hueic.kenhoang.orderfoods_app.model.Order;

/**
 * Created by kenhoang on 28/01/2018.
 */

public class Database extends SQLiteAssetHelper {
    private static final String DB_NAME = "FoodFastDB";
    private static final int DB_VER = 1;
    public Database(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    public List<Order> getCarts() {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String[] sqlSelect = {"ProductId", "ProductName", "Quantity", "Price", "Discount"};
        String sqlTable    = "OrderDetail";

        qb.setTables(sqlTable);

        Cursor cursor = qb.query(db, sqlSelect, null, null, null, null, null);
        final List<Order> result = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                result.add(new Order(cursor.getString(cursor.getColumnIndex(sqlSelect[0])),
                        cursor.getString(cursor.getColumnIndex(sqlSelect[1])),
                        cursor.getString(cursor.getColumnIndex(sqlSelect[2])),
                        cursor.getString(cursor.getColumnIndex(sqlSelect[3])),
                        cursor.getString(cursor.getColumnIndex(sqlSelect[4]))));
            } while (cursor.moveToNext());
        }
        return result;
    }

    public void addToCart(Order order) {
        // INSERT INTO OrderDetail (ProductId, ProductName, Quantity, Price, Discount)
        // VALUES ('%s', '%s', '%s', '%s', '%s');
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("INSERT INTO OrderDetail (ProductId, ProductName, Quantity, Price, Discount) VALUES ('%s', '%s', '%s', '%s', '%s');",
                order.getProductId(),
                order.getProductName(),
                order.getQuantity(),
                order.getPrice(),
                order.getDiscount());
        db.execSQL(query);
    }

    public void cleanCart() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "DELETE FROM OrderDetail";
        db.execSQL(query);
    }
}
