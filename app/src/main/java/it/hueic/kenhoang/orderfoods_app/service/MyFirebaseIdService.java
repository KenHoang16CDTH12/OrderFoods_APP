package it.hueic.kenhoang.orderfoods_app.service;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import it.hueic.kenhoang.orderfoods_app.common.Common;
import it.hueic.kenhoang.orderfoods_app.model.Token;

/**
 * Created by kenhoang on 09/02/2018.
 */

public class MyFirebaseIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String tokenRefreshed = FirebaseInstanceId.getInstance().getToken();
        if (Common.currentUser != null) updatetokenToFirebase(tokenRefreshed);
    }

    private void updatetokenToFirebase(String tokenRefreshed) {
        DatabaseReference tokenDB = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(tokenRefreshed, false); //false because this token send from Client app
        tokenDB.child(Common.currentUser.getPhone()).setValue(token);

    }
}
