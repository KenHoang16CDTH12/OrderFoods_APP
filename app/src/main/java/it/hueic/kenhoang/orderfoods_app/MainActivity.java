package it.hueic.kenhoang.orderfoods_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Typeface;
import android.icu.text.TimeZoneFormat;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.paperdb.Paper;
import it.hueic.kenhoang.orderfoods_app.common.Common;
import it.hueic.kenhoang.orderfoods_app.model.User;

public class MainActivity extends AppCompatActivity {
    private Button mBtnSignIn, mBtnSignUp;
    private TextView tvSlogan;
    DatabaseReference mDataUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        //Facebook SDK
        //printKeyHash();
        //InitViews
        initViews();
        //Init Paper
        Paper.init(this);
        //InitEvents
        initEvents();
        //CheckRemember
        checkRemember();
    }

    private void printKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("it.hueic.kenhoang.orderfoods_app",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature: info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", "printKeyHash: " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void initEvents() {
        mBtnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(signInIntent);
            }
        });

        mBtnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signUpIntent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(signUpIntent);
            }
        });
    }

    private void checkRemember() {
        //Check remember
        String user = Paper.book().read(Common.USER_KEY);
        String pwd = Paper.book().read(Common.PWD_KEY);

        if (user != null & pwd != null) {
            if (!user.isEmpty() && !pwd.isEmpty()) login(user, pwd);
        }
    }
    /**
     * Auto login
     * @param phone
     * @param pwd
     */
    private void login(final String phone, final String pwd) {
        //Just copy login code from SignInActivity
        //InitFireBase
        mDataUser = FirebaseDatabase.getInstance().getReference("User");
        if (Common.isConnectedToInternet(getBaseContext())) {
            final ProgressDialog mProgressbar = new ProgressDialog(MainActivity.this);
            mProgressbar.setMessage("Logging ...");
            mProgressbar.show();
            mDataUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Check if user not exist in database
                    if (dataSnapshot.child(phone).exists()) {
                        //Get User information
                        mProgressbar.dismiss();
                        User user = dataSnapshot
                                .child(phone)
                                .getValue(User.class);
                        if (user.getPassword().equals(pwd)) {
                            user.setPhone(phone);//Set phone
                            Intent homeIntent = new Intent(MainActivity.this, HomeActivity.class);
                            Common.currentUser = user;
                            startActivity(homeIntent);
                            finish();
                        } else {
                            Snackbar.make(findViewById(R.id.relSignInMain), "Wrong password ...", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        mProgressbar.dismiss();
                        Snackbar.make(findViewById(R.id.relSignInMain), "User not exists database ...", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            MDToast.makeText(MainActivity.this, "Please check your connection ...", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
            return;
        }
    }

    private void initViews() {
        tvSlogan   = findViewById(R.id.tvSlogan);
        mBtnSignIn = findViewById(R.id.btnSignIn);
        mBtnSignUp = findViewById(R.id.btnSignUp);
        tvSlogan.setTypeface(Common.setNabiLaFont(this));
    }
}
