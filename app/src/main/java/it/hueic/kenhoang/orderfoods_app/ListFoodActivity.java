package it.hueic.kenhoang.orderfoods_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

import it.hueic.kenhoang.orderfoods_app.Interface.ItemClickListener;
import it.hueic.kenhoang.orderfoods_app.ViewHolder.FoodVIewHolder;
import it.hueic.kenhoang.orderfoods_app.model.Food;

public class ListFoodActivity extends AppCompatActivity {
    private static final String TAG = ListFoodActivity.class.getSimpleName();
    private TextView tvTitle;
    private RecyclerView recycler_food;
    private RecyclerView.LayoutManager mLayoutManager;
    DatabaseReference mFoodData;
    String categoryId = "";
    FirebaseRecyclerAdapter<Food, FoodVIewHolder> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setContentView(R.layout.activity_list_food);
        //InitFireBase
        mFoodData   = FirebaseDatabase.getInstance().getReference().child("Foods");
        //InitView
        initView();
        //Get Intent here
        if (getIntent() != null) categoryId = getIntent().getStringExtra("CategoryId");
        if (!categoryId.isEmpty() && categoryId != null) loadListFood(categoryId);
    }

    private void loadListFood(String categoryId) {
        adapter = new FirebaseRecyclerAdapter<Food, FoodVIewHolder>(
                Food.class,
                R.layout.item_food_ref_menu,
                FoodVIewHolder.class,
                mFoodData.orderByChild("MenuId").equalTo(categoryId)
                //Like: Select * From Foods where MenuId = ?
        ) {
            @Override
            protected void populateViewHolder(FoodVIewHolder viewHolder, Food model, int position) {
                viewHolder.tvFoodName.setText(model.getName());
                Picasso.with(getBaseContext())
                        .load(model.getImage())
                        .into(viewHolder.imgFood);
                final Food local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Toast.makeText(ListFoodActivity.this, "" + local.getName(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
        //Set Adapter
        recycler_food.setAdapter(adapter);
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        tvTitle         = findViewById(R.id.tvTitle);
        tvTitle.setText("Food");
        setSupportActionBar(toolbar);
        recycler_food   = findViewById(R.id.recycler_food);
        recycler_food.setHasFixedSize(true);
        mLayoutManager  = new LinearLayoutManager(this);
        recycler_food.setLayoutManager(mLayoutManager);
    }
}
