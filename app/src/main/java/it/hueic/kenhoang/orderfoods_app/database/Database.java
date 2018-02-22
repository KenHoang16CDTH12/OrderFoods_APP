package it.hueic.kenhoang.orderfoods_app.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

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

        String[] sqlSelect = {"ID", "ProductId", "ProductName", "Quantity", "Price", "Discount", "Image"};
        String sqlTable    = "OrderDetail";

        qb.setTables(sqlTable);

        Cursor cursor = qb.query(db, sqlSelect, null, null, null, null, null);
        final List<Order> result = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                result.add(new Order(
                        cursor.getInt(cursor.getColumnIndex(sqlSelect[0])),
                        cursor.getString(cursor.getColumnIndex(sqlSelect[1])),
                        cursor.getString(cursor.getColumnIndex(sqlSelect[2])),
                        cursor.getString(cursor.getColumnIndex(sqlSelect[3])),
                        cursor.getString(cursor.getColumnIndex(sqlSelect[4])),
                        cursor.getString(cursor.getColumnIndex(sqlSelect[5])),
                        cursor.getString(cursor.getColumnIndex(sqlSelect[6]))
                ));
            } while (cursor.moveToNext());
        }
        return result;
    }

    public void addToCart(Order order) {
        // INSERT INTO OrderDetail (ProductId, ProductName, Quantity, Price, Discount)
        // VALUES ('%s', '%s', '%s', '%s', '%s');
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("INSERT INTO OrderDetail (ProductId, ProductName, Quantity, Price, Discount, Image) VALUES ('%s', '%s', '%s', '%s', '%s', '%s');",
                order.getProductId(),
                order.getProductName(),
                order.getQuantity(),
                order.getPrice(),
                order.getDiscount(),
                order.getImage());
        try {
            db.execSQL(query);
        } catch (Exception ex) {
            Log.e("ERROR", ex.getMessage());
        }

    }

    public void cleanCart() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "DELETE FROM OrderDetail";
        db.execSQL(query);
    }

    //Favorites
    public void addToFavorites(String foodId, String userPhone) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("INSERT INTO Favorites(FoodId, UserPhone) VALUES ('%s', '%s');", foodId, userPhone);
        db.execSQL(query);
    }

    public void removeFromFavorites(String foodId, String userPhone) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("DELETE FROM Favorites WHERE FoodId = '%s' AND UserPhone = '%s';", foodId, userPhone);
        db.execSQL(query);
    }

    public boolean isFavorite(String foodId, String userPhone) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("SELECT * FROM Favorites WHERE FoodId='%s' AND UserPhone = '%s';", foodId, userPhone);
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public List<String> getIdFoods() {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String[] sqlSelect = {"FoodId"};
        String sqlTable    = "Favorites";

        qb.setTables(sqlTable);

        Cursor cursor = qb.query(db, sqlSelect, null, null, null, null, null);
        final List<String> result = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                result.add(cursor.getString(cursor.getColumnIndex(sqlSelect[0])));

            } while (cursor.moveToNext());
        }
        return result;
    }

    public int getCountCart() {
        int count = 0;
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("SELECT COUNT(*) FROM OrderDetail");
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                count = cursor.getInt(0);
            } while (cursor.moveToNext());
        }
        return count;
    }

    public void updateCart(Order order) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("UPDATE OrderDetail SET Quantity= %s WHERE ID = %d", order.getQuantity(), order.getID());
        db.execSQL(query);
    }
}
