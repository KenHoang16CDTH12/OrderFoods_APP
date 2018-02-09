package it.hueic.kenhoang.orderfoods_app.common;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import it.hueic.kenhoang.orderfoods_app.R;
import it.hueic.kenhoang.orderfoods_app.model.User;
import it.hueic.kenhoang.orderfoods_app.remote.APIService;
import it.hueic.kenhoang.orderfoods_app.remote.RetrofitClient;

/**
 * Created by kenhoang on 26/01/2018.
 */

public class Common {
    public static User currentUser;
    public static String nameProduct = "";
    public static final String DELETE = "Delete";
    public static final String USER_KEY = "User";
    public static final String PWD_KEY = "Password";

    private static final String BASE_URL = "https://fcm.googleapis.com/";

    public static APIService getFCMService() {
        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }

    public static String convertCodeToStatus(String status) {
        String result = "";
        switch (status) {
            case "0":
                result = "Placed";
                break;
            case "1":
                result = "On my way";
                break;
            case "2":
                result = "Shipped";
                break;
            default:
                result = "Error";
                break;
        }
        return result;
    }

    /**
     * Check connect internet (connected == true)
     * @param context
     * @return
     */
    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) return true;
                }
            }
        }
        return false;
    }

    public static void showSnackBar(String msg, Activity context, View view){
        Snackbar.make(context.findViewById(view.getId()), msg, Toast.LENGTH_SHORT)
                .show();
    }


}
