package it.hueic.kenhoang.orderfoods_app;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.util.ArrayList;
import java.util.List;

import it.hueic.kenhoang.orderfoods_app.adapter.FavoriteAdapter;
import it.hueic.kenhoang.orderfoods_app.common.Common;
import it.hueic.kenhoang.orderfoods_app.database.Database;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ListFavoriteFoodActivity extends AppCompatActivity {
    private static final String TAG = ListFavoriteFoodActivity.class.getSimpleName();
    private RecyclerView recycler_food;
    private RecyclerView.LayoutManager mLayoutManager;
    List<String> listIdFood = new ArrayList<>();
    Database localDB;
    FavoriteAdapter adapter;
    DatabaseReference foodData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        //Notes : add this code before setContentView
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/food_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_list_favorite_food);
        //InitFirebase
        foodData = FirebaseDatabase.getInstance().getReference("Foods");
        //Local DB
        localDB = new Database(this);
        listIdFood = localDB.getIdFoods();
        //InitView
        initView();
        //Check connect internet
        if (Common.isConnectedToInternet(getBaseContext())) loadListFood(listIdFood);
        else {
            MDToast.makeText(this, "Please check your connection ...", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
            return;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void loadListFood(List<String> listIdFood) {
        adapter = new FavoriteAdapter(listIdFood, this);
        recycler_food.setAdapter(adapter);
    }

    /**
     * FindViewByID setup Recycle
     */
    private void initView() {
        recycler_food   = findViewById(R.id.recycler_food);
        recycler_food.setHasFixedSize(true);
        mLayoutManager  = new LinearLayoutManager(this);
        recycler_food.setLayoutManager(mLayoutManager);
    }
}
