package it.hueic.kenhoang.orderfoods_app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import it.hueic.kenhoang.orderfoods_app.Interface.ItemClickListener;
import it.hueic.kenhoang.orderfoods_app.adapter.ViewHolder.MenuViewHolder;
import it.hueic.kenhoang.orderfoods_app.common.Common;
import it.hueic.kenhoang.orderfoods_app.model.Category;
import it.hueic.kenhoang.orderfoods_app.model.Token;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    //View
    TextView tvFullName, tvTitle;
    private DatabaseReference mCategoryData;
    private RecyclerView recycler_menu;
    private RecyclerView.LayoutManager mLayoutManger;
    private FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean statusItemList = false;
    Menu menu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        //Notes : add this code before setContentView
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        tvTitle         = findViewById(R.id.tvTitle);
        tvTitle.setText("Menu");
        setSupportActionBar(toolbar);
        //Init FireBase
        mCategoryData   = FirebaseDatabase.getInstance().getReference("Category");
        //Create adapter
        createAdapter();
        //Init Paper
        Paper.init(this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cartIntent = new Intent(HomeActivity.this, CartActivity.class);
                startActivity(cartIntent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //Set name for user
        View headerView = navigationView.getHeaderView(0);
        tvFullName      = headerView.findViewById(R.id.tvFullName);
        tvFullName.setText(Common.currentUser.getName());
        //Load menu
        recycler_menu    = findViewById(R.id.recycler_menu);
        mLayoutManger   = new LinearLayoutManager(this);
        if (statusItemList) {
            recycler_menu.setHasFixedSize(true);
            recycler_menu.setLayoutManager(mLayoutManger);
        } else {
            recycler_menu.setLayoutManager(new GridLayoutManager(this, 2));
        }
        //Add animation recyclerview
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recycler_menu.getContext(),
                R.anim.layout_fall_down);
        recycler_menu.setLayoutAnimation(controller);
        //SwipeRefresh Layout
        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark
        );
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                checkLoadMenuSwipe();

            }
        });
        //Default, load for first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                checkLoadMenuSwipe();
            }
        });

        if (Common.currentUser != null) updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    private void checkLoadMenuSwipe() {
        //Check connect internet
        if (Common.isConnectedToInternet(this)) loadMenu();
        else {
            MDToast.makeText(HomeActivity.this, "Please check your connection ...", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
            return;
        }
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
    private void updateToken(String token) {
            DatabaseReference tokenDB = FirebaseDatabase.getInstance().getReference("Tokens");
            Token data = new Token(token, false); //false because this token send from Client app
            tokenDB.child(Common.currentUser.getPhone()).setValue(data);
    }

    /**
     * Create adapter
     */

    private void createAdapter() {

        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(mCategoryData, Category.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder viewHolder, int position, @NonNull Category model) {
                viewHolder.tvMenuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.imgMenu);
                final Category clickItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Get CategoryId and send to new Activity
                        Intent foodListIntent = new Intent(HomeActivity.this, ListFoodActivity.class);
                        //Because CategoryId is key, so we just get key of this item
                        foodListIntent.putExtra("CategoryId", adapter.getRef(position).getKey());
                        startActivity(foodListIntent);
                    }
                });
            }

            @Override
            public MenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(statusItemList ? R.layout.item_category_menu : R.layout.item_category_menu_grid, parent, false);
                return new MenuViewHolder(itemView);
            }
        };
    }
    /**
     * Load Menu
     */
    private void loadMenu() {
        adapter.startListening();
        //Animation
        recycler_menu.scheduleLayoutAnimation();
        recycler_menu.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_view) {
            statusItemList = !statusItemList;
            if (statusItemList) {
                menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.icon_view_list));
                recycler_menu.setHasFixedSize(true);
                recycler_menu.setLayoutManager(mLayoutManger);
            } else {
                menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.icon_view_grid));
                recycler_menu.setLayoutManager(new GridLayoutManager(this, 2));
            }
            checkLoadMenuSwipe();
        }
        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {
            if (Common.isConnectedToInternet(this)) loadMenu();
            else {
                MDToast.makeText(HomeActivity.this, "Please check your connection ...", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
            }
        } else if (id == R.id.nav_fav) {
            startActivity(new Intent(HomeActivity.this, ListFavoriteFoodActivity.class));
        }
          else if (id == R.id.nav_cart) {
            startActivity(new Intent(HomeActivity.this, CartActivity.class));
        } else if (id == R.id.nav_orders) {
            startActivity(new Intent(HomeActivity.this, OrderStatusActivity.class));
        } else if (id == R.id.nav_change_pass) {
            //Change password
            showChangePasswordDialog();
        } else if (id == R.id.nav_log_out) {
            //Remove remember user & password
            Paper.book().destroy();
            //Logout
            Intent signInIntent = new Intent(HomeActivity.this, SignInActivity.class);
            signInIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signInIntent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Change password dialog
     */
    private void showChangePasswordDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
        alertDialog.setTitle("CHANGE PASSWORD");
        alertDialog.setMessage("Please fill all information.");
        alertDialog.setIcon(R.drawable.ic_security_black_24dp);
        View dialog_change_password = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        alertDialog.setView(dialog_change_password);

        final MaterialEditText edPass = dialog_change_password.findViewById(R.id.edPass);
        final MaterialEditText edNewPass = dialog_change_password.findViewById(R.id.edNewPass);
        final MaterialEditText edRepeatPass = dialog_change_password.findViewById(R.id.edRepeatPass);

        //Button
        alertDialog.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Change password here

                //For use SpotsDialog, please use AlertDialog From android.app, not from v7 like above AlertDialog
                final AlertDialog waitingDialog = new SpotsDialog(HomeActivity.this);
                waitingDialog.show();

                //Check old password
                if (edPass.getText().toString().equals(Common.currentUser.getPassword())) {
                    //Check new password and repeat password
                    if (edNewPass.getText().toString().equals(edRepeatPass.getText().toString())) {
                        Map<String, Object> passwordUpdate = new HashMap<>();
                        passwordUpdate.put("password", edNewPass.getText().toString());
                        //Make update
                        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference("User");
                        mUserDB.child(Common.currentUser.getPhone())
                                .updateChildren(passwordUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        waitingDialog.dismiss();
                                        MDToast.makeText(HomeActivity.this, "Password was update ", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        waitingDialog.dismiss();
                                        MDToast.makeText(HomeActivity.this, "ERROR " + e.getMessage(), MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                                    }
                                });
                    } else {
                        waitingDialog.dismiss();
                        MDToast.makeText(HomeActivity.this, "New password doesn't match! ", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
                    }
                } else {
                    waitingDialog.dismiss();
                    MDToast.makeText(HomeActivity.this, "Wrong old password", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                }
            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();
    }

}
