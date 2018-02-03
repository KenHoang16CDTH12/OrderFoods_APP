package it.hueic.kenhoang.orderfoods_app;

import android.app.ProgressDialog;
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

public class SignUpActivity extends AppCompatActivity {
    private MaterialEditText edPhone, edPass, edName;
    private ProgressDialog mProgressbar;
    private Button btnSignUp;
    private DatabaseReference mDataUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        //InitViews
        initViews();
        //InitFireBase
        mDataUser = FirebaseDatabase.getInstance().getReference("User");
        //InitEvents
        initEvents();
    }

    private void initEvents() {
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    mProgressbar.setMessage("Please waiting ....");
                    mProgressbar.show();
                    mDataUser.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child(edPhone.getText().toString()).exists()) {
                                mProgressbar.dismiss();
                                Snackbar.make(findViewById(R.id.relSignUpMain), "Phone number already register ...", Toast.LENGTH_SHORT).show();
                            } else {
                                mProgressbar.dismiss();
                                User user = new User(edName.getText().toString(), edPass.getText().toString());
                                mDataUser.child(edPhone.getText().toString()).setValue(user);
                                Toast.makeText(getBaseContext(), "Sign up successfully ...", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    MDToast.makeText(SignUpActivity.this, "Please check your connection ...", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
                    return;
                }
            }
        });
    }

    private void initViews() {
        edName          = findViewById(R.id.edName);
        edPhone         = findViewById(R.id.edPhone);
        edPass          = findViewById(R.id.edPass);
        btnSignUp       = findViewById(R.id.btnSignUp);
        mProgressbar    = new ProgressDialog(this);
    }
}
