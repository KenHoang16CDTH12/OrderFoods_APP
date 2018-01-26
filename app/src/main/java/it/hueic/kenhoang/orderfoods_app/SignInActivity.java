package it.hueic.kenhoang.orderfoods_app;

import android.app.ProgressDialog;
import android.content.Intent;
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
                                Intent homeIntent = new Intent(SignInActivity.this, HomeActivity.class);;
                                Common.currentUser = user;
                                startActivity(homeIntent);
                                finish();
                            } else {
                                Toast.makeText(SignInActivity.this, "Wrong password ...", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            mProgressbar.dismiss();
                            Toast.makeText(SignInActivity.this, "User not exist in database", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
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
