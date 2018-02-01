package it.hueic.kenhoang.orderfoods_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import it.hueic.kenhoang.orderfoods_app.Interface.ItemClickListener;
import it.hueic.kenhoang.orderfoods_app.adapter.ViewHolder.FoodVIewHolder;
import it.hueic.kenhoang.orderfoods_app.model.Food;

public class ListFoodActivity extends AppCompatActivity {
    private static final String TAG = ListFoodActivity.class.getSimpleName();
    private RecyclerView recycler_food;
    private RecyclerView.LayoutManager mLayoutManager;
    DatabaseReference mFoodData;
    String categoryId = "";
    FirebaseRecyclerAdapter<Food, FoodVIewHolder> adapter;
    //Search Functionality
    FirebaseRecyclerAdapter<Food, FoodVIewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;
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
        //SearchBar
        handleSearchBar();
    }

    private void handleSearchBar() {
        materialSearchBar = findViewById(R.id.searchBar);
        materialSearchBar.setHint("Enter your food");
        loadSuggest();
        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //When user type their text, we will change suggest list
                List<String> suggest = new ArrayList<>();
                for(String search: suggestList) { //Loop in suggest List
                    if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                        suggest.add(search);
                }
                materialSearchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                //When Search Bar is close
                //Restore original adapter
                if (!enabled) recycler_food.setAdapter(adapter);
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                //When search finish
                //Show result of search adapter
                if (!TextUtils.isEmpty(text)) startSearch(text);
                else recycler_food.setAdapter(adapter);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });
    }

    /**
     * Adapter search
     * @param text
     */
    private void startSearch(CharSequence text) {
        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodVIewHolder>(
                Food.class,
                R.layout.item_food_ref_menu,
                FoodVIewHolder.class,
                mFoodData.orderByChild("name").equalTo(String.valueOf(text)) //Compare name
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
                        //Start new Activity
                        Intent foodDetailIntent = new Intent(ListFoodActivity.this, FoodDetailActivity.class);
                        foodDetailIntent.putExtra("FoodId", searchAdapter.getRef(position).getKey()); //Send Food Id to new activity
                        startActivity(foodDetailIntent);
                    }
                });
            }
        };
        recycler_food.setAdapter(searchAdapter);//Set adapter for Recycler View is Search result
    }

    /**
     * Fill data search bar
     */
    private void loadSuggest() {
        mFoodData.orderByChild("menuId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            Food item = snapshot.getValue(Food.class);
                            suggestList.add(item.getName());// Add name of food to suggest list
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void loadListFood(String categoryId) {
        adapter = new FirebaseRecyclerAdapter<Food, FoodVIewHolder>(
                Food.class,
                R.layout.item_food_ref_menu,
                FoodVIewHolder.class,
                mFoodData.orderByChild("menuId").equalTo(categoryId)
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
                        //Start new Activity
                        Intent foodDetailIntent = new Intent(ListFoodActivity.this, FoodDetailActivity.class);
                        foodDetailIntent.putExtra("FoodId", adapter.getRef(position).getKey()); //Send Food Id to new activity
                        startActivity(foodDetailIntent);
                    }
                });
            }
        };
        //Set Adapter
        recycler_food.setAdapter(adapter);
    }

    private void initView() {
        recycler_food   = findViewById(R.id.recycler_food);
        recycler_food.setHasFixedSize(true);
        mLayoutManager  = new LinearLayoutManager(this);
        recycler_food.setLayoutManager(mLayoutManager);
    }
}
