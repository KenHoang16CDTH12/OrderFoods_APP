package it.hueic.kenhoang.orderfoods_app;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.valdesekamdem.library.mdtoast.MDToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import it.hueic.kenhoang.orderfoods_app.adapter.CartAdapter;
import it.hueic.kenhoang.orderfoods_app.common.Common;
import it.hueic.kenhoang.orderfoods_app.database.Database;
import it.hueic.kenhoang.orderfoods_app.model.MyReponse;
import it.hueic.kenhoang.orderfoods_app.model.NotificationModel;
import it.hueic.kenhoang.orderfoods_app.model.Order;
import it.hueic.kenhoang.orderfoods_app.model.Request;
import it.hueic.kenhoang.orderfoods_app.model.Sender;
import it.hueic.kenhoang.orderfoods_app.model.Token;
import it.hueic.kenhoang.orderfoods_app.remote.APIService;
import it.hueic.kenhoang.orderfoods_app.remote.IGoogleService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CartActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{
    private static final String TAG = CartActivity.class.getSimpleName();
    RecyclerView listCarts;
    RecyclerView.LayoutManager mLayoutManager;
    RelativeLayout relMainCart;
    public TextView tvTotalPrice, tvTitle;
    Button btnPlace;

    DatabaseReference mDataRequest;

    List<Order> carts = new ArrayList<>();

    CartAdapter adapter;

    APIService mService;
    // Address
    Place shippingAddress;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 3000;
    private static final int DISPLACEMENT = 10;

    private static final int LOCATION_REQUEST_CODE = 71;
    private static final int PLAY_SERVICE_REQUEST = 72;

    //Declare Google Map API Retrofit
    IGoogleService mGoogleService;

    HashMap<String, String> address = new HashMap<>();

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
        setContentView(R.layout.activity_cart);
        //Request runtime permission
        requestRuntimePermission();
        //InitService
        mService = Common.getFCMService();
        mGoogleService = Common.getGoogleMapService();
        //InitFireBase
        mDataRequest = FirebaseDatabase.getInstance().getReference("Requests");
        //InitView
        initView();
        //Load Data List
        loadListCart();
        //InitEvent
        initEvent();
    }

    /**
     * Request permission
     */
    private void requestRuntimePermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_REQUEST_CODE);
        } else {
            if (checkPlayService()) { //If have play service on device
                buildGoogleApiClient();
                createLocationRequest();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayService()) { //If have play service on device
                        buildGoogleApiClient();
                        createLocationRequest();
                    }
                }
                break;
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayService() {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GoogleApiAvailability.getInstance().isUserResolvableError(resultCode))
                GoogleApiAvailability.getInstance().getErrorDialog(this, resultCode, PLAY_SERVICE_REQUEST).show();
            else {
                MDToast.makeText(this, "This device is not supported", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                finish();
            }
            return false;
        }
        return true ;
    }

    /**
     * Change font
     * @param newBase
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    /**
     * Load list card from database
     */
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

    /**
     * Inits Event
     */
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
        final RadioButton radShipToAddress = dialog_address_comment.findViewById(R.id.radShipToAddress);
        final RadioButton radHomeAddress = dialog_address_comment.findViewById(R.id.radHomeAddress);
        final MaterialEditText edComment = dialog_address_comment.findViewById(R.id.edComment);
        final PlaceAutocompleteFragment edAddress = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        //Hide search icon before fragment
        edAddress.getView().findViewById(R.id.place_autocomplete_search_button)
                .setVisibility(View.GONE);
        //Set hint for autocomplete edit text
        ((EditText) edAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setHint("Enter your address");
        ((EditText) edAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setTextSize(14);
        //Event radio button
        radShipToAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Ship to the address features
                if (isChecked) { //isChecked == true
                    mGoogleService.getAddressName(String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%s,%s&sensor=false",
                            String.valueOf(mLastLocation.getLatitude()),
                            String.valueOf(mLastLocation.getLongitude())))
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    //If fetch API ok
                                    try {
                                        JSONObject jsonObject = new JSONObject(response.body().toString());
                                        JSONArray resultsArray = jsonObject.getJSONArray("results");
                                        JSONObject firstObject = resultsArray.getJSONObject(0);
                                        String formatted_address = firstObject.getString("formatted_address");
                                        String lat = firstObject
                                                .getJSONObject("geometry")
                                                .getJSONObject("location")
                                                .get("lat").toString();
                                        String lng = firstObject
                                                .getJSONObject("geometry")
                                                .getJSONObject("location")
                                                .get("lng").toString();
                                        address.put("address", formatted_address);
                                        address.put("lat", lat);
                                        address.put("lng", lng);
                                        //Set this address to edAddress
                                        ((EditText) edAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                                                .setText(address.get("address"));

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    MDToast.makeText(CartActivity.this, "Error " + t.getMessage(), MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                                }
                            });
                }
            }
        });
        // Get address from places autocomplete
        edAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                shippingAddress = place;
            }

            @Override
            public void onError(Status status) {
                Log.e(TAG, "onError: " + status.getStatusMessage());
            }
        });
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);
        alertDialog.setView(dialog_address_comment);
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Add check condition here
                //If user select address from Place fragment, just use it
                //If user select ship to this address, get Address from location and use it
                //If use select Home Address, get HomeAddress from Profile and use it
                if (!radShipToAddress.isChecked() && !radHomeAddress.isChecked()) {
                    //If both radio is not selected ->
                    if (shippingAddress != null) {
                        address.put("address", shippingAddress.getAddress().toString());
                        address.put("lat", String.valueOf(shippingAddress.getLatLng().latitude));
                        address.put("lng", String.valueOf(shippingAddress.getLatLng().longitude));
                    } else {
                        MDToast.makeText(CartActivity.this, "Please enter address or select option address", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
                        //Fix crash fragment
                        removeFragmentToFix();
                        return;
                    }
                }
                if (address.isEmpty()) {
                    MDToast.makeText(CartActivity.this, "Please enter address or select option address", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
                    //Fix crash fragment
                    removeFragmentToFix();
                    return;
                }
                //Create new Request
                Request request = new Request(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                        address.get("address"),
                        tvTotalPrice.getText().toString(),
                        "0",//Status
                        edComment.getText().toString().trim(),
                        String.format("%s,%s", address.get("lat"), address.get("lng")),
                        carts
                );
                //Submit to FireBase
                //We will using System.currentMilli to key
                //Delete cart
                String order_number = String.valueOf(System.currentTimeMillis());
                mDataRequest.child(String.valueOf(System.currentTimeMillis()))
                            .setValue(request);
                new Database(getBaseContext()).cleanCart();
                loadListCart();
                adapter.notifyDataSetChanged();
                listCarts.setAdapter(adapter);
                sendNotificationOrder(order_number);
                dialogInterface.dismiss();
                //Remove fragment
                removeFragmentToFix();
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                //Remove Fragment
                removeFragmentToFix();
            }
        });

        alertDialog.show();
    }

    /**
     * Remove fragment after dialog dismiss
     */
    private void removeFragmentToFix() {
        //Fix crash fragment
        //Remove fragment
        getFragmentManager().beginTransaction()
                .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                .commit();
    }

    /**
     * Send notificaction Order
     * @param order_number
     */
    private void sendNotificationOrder(final String order_number) {
        DatabaseReference tokenDB = FirebaseDatabase.getInstance().getReference("Tokens");
        final Query data = tokenDB.orderByChild("serverToken").equalTo(true);//Get all node with is Servertoken is true
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot: dataSnapshot.getChildren()) {
                    Token serverToken = postSnapShot.getValue(Token.class);

                    //Create raw payload to send
                    NotificationModel notificationModel = new NotificationModel("Ken Hoang", "You have new order #" + order_number);
                    Sender content = new Sender(serverToken.getToken(), notificationModel);

                    mService.sendNotification(content)
                            .enqueue(new Callback<MyReponse>() {
                                @Override
                                public void onResponse(Call<MyReponse> call, Response<MyReponse> response) {
                                    //Only run when get result
                                    if (response.code() == 200) {
                                        if (response.body().sucess == 0) {
                                            Snackbar.make(relMainCart, "Thank you, Order Place", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Snackbar.make(relMainCart, "Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyReponse> call, Throwable t) {
                                    Log.e("ERROR", t.getMessage());
                                }
                            });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Inits View
     */
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

    /**
     * Context Menu Item
     * @param item
     * @return
     */
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d(TAG, "displayLocation: " + mLastLocation.getLatitude() + "/" + mLastLocation.getLongitude());
        } else {
            Log.d(TAG, "displayLocation: Could not get your location");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }
}
