package it.hueic.kenhoang.orderfoods_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.valdesekamdem.library.mdtoast.MDToast;

import it.hueic.kenhoang.orderfoods_app.common.Common;
import it.hueic.kenhoang.orderfoods_app.model.User;

public class SignInActivity extends AppCompatActivity {
    private MaterialEditText edPhone, edPass;
    private ProgressDialog mProgressbar;
    private Button btnSignIn;
    private DatabaseReference mDataUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        //InitViews
        initViews();
        //InitFireBase
        mDataUser = FirebaseDatabase.getInstance().getReference("User");
        //InitEvents
        initEvents();
    }

    private void initEvents() {
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    mProgressbar.setMessage("Logging ...");
                    mProgressbar.show();
                    mDataUser.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Check if user not exist in database
                            if (dataSnapshot.child(edPhone.getText().toString()).exists()) {
                                //Get User information
                                mProgressbar.dismiss();
                                User user = dataSnapshot
                                        .child(edPhone.getText().toString())
                                        .getValue(User.class);
                                if (user.getPassword().equals(edPass.getText().toString())) {
                                    user.setPhone(edPhone.getText().toString());//Set phone
                                    Intent homeIntent = new Intent(SignInActivity.this, HomeActivity.class);
                                    ;
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
                    MDToast.makeText(SignInActivity.this, "Please check your connection ...", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
                    return;
                }
            }
        });
    }

    private void initViews() {
        edPhone         = findViewById(R.id.edPhone);
        edPass          = findViewById(R.id.edPass);
        btnSignIn       = findViewById(R.id.btnSignIn);
        mProgressbar    = new ProgressDialog(this);
    }
}
