package it.hueic.kenhoang.orderfoods_app;

import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.hueic.kenhoang.orderfoods_app.adapter.CartAdapter;
import it.hueic.kenhoang.orderfoods_app.common.Common;
import it.hueic.kenhoang.orderfoods_app.database.Database;
import it.hueic.kenhoang.orderfoods_app.model.Order;
import it.hueic.kenhoang.orderfoods_app.model.Request;

public class CartActivity extends AppCompatActivity {
    RecyclerView listCarts;
    RecyclerView.LayoutManager mLayoutManager;
    RelativeLayout relMainCart;
    TextView tvTotalPrice, tvTitle;
    Button btnPlace;

    DatabaseReference mDataRequest;

    List<Order> carts = new ArrayList<>();

    CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setContentView(R.layout.activity_cart);
        //InitFireBase
        mDataRequest = FirebaseDatabase.getInstance().getReference("Requests");
        //InitView
        initView();
        //Load Data List
        loadListCart();
        //InitEvent
        initEvent();
    }

    private void loadListCart() {
        carts = new Database(this).getCarts();
        adapter = new CartAdapter(carts, this);
        adapter.notifyDataSetChanged();
        listCarts.setAdapter(adapter);
        //Calculate total price
        int total = 0;
        for (Order order: carts)
            total += (Integer.parseInt(order.getPrice())) * (Integer.parseInt(order.getQuantity()));
        Locale locale = new Locale("en", "US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        tvTotalPrice.setText(fmt.format(total));
    }

    private void initEvent() {
        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!carts.isEmpty()) showAlertDialog();
                else  Snackbar.make(relMainCart, "Cart is empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAlertDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(CartActivity.this);
        alertDialog.setTitle("One more step!");
        alertDialog.setMessage("Enter your address: ");

        LayoutInflater inflater = this.getLayoutInflater();
        View dialog_address_comment = inflater.inflate(R.layout.dialog_order_comment_address, null);
        final MaterialEditText edAddress = dialog_address_comment.findViewById(R.id.edAddress);
        final MaterialEditText edComment = dialog_address_comment.findViewById(R.id.edComment);


        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);
        alertDialog.setView(dialog_address_comment);
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Create new Request
                Request request = new Request(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                        edAddress.getText().toString().trim(),
                        tvTotalPrice.getText().toString(),
                        "0",//Status
                        edComment.getText().toString().trim(),
                        carts
                );
                //Submit to FireBase
                //We will using System.currentMilli to key
                //Delete cart
                mDataRequest.child(String.valueOf(System.currentTimeMillis()))
                            .setValue(request);
                new Database(getBaseContext()).cleanCart();
                loadListCart();
                adapter.notifyDataSetChanged();
                listCarts.setAdapter(adapter);
                Snackbar.make(relMainCart, "Thank you, Order Place", Toast.LENGTH_SHORT).show();
                dialogInterface.dismiss();
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        tvTitle         = findViewById(R.id.tvTitle);
        tvTitle.setText("Cart List");
        setSupportActionBar(toolbar);
        listCarts = findViewById(R.id.recycler_cart);
        listCarts.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        listCarts.setLayoutManager(mLayoutManager);
        relMainCart = findViewById(R.id.relMainCart);
        tvTotalPrice = findViewById(R.id.total);
        btnPlace = findViewById(R.id.btnPlaceOrder);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.DELETE)) deleteCart(item.getOrder());

        return super.onContextItemSelected(item);
    }

    /**
     * Delete carts in SQLite
     * @param position
     */
    private void deleteCart(int position) {
        //We will remove item at List<Order> by position
        carts.remove(position);
        //After that, we will delete all old data from SQLite
        new Database(this).cleanCart();
        //And final, we will update new data from List<Order> to SQLite
        for (Order item: carts) new Database(this).addToCart(item);
        //Refresh
        loadListCart();
    }
}
