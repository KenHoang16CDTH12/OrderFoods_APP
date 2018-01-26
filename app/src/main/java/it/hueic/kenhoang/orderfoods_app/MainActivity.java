package it.hueic.kenhoang.orderfoods_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button mBtnSignIn, mBtnSignUp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setContentView(R.layout.activity_main);
        //InitViews
        initViews();
        //InitEvents
        initEvents();
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

    private void initViews() {
        mBtnSignIn = findViewById(R.id.btnSignIn);
        mBtnSignUp = findViewById(R.id.btnSignUp);
    }
}
