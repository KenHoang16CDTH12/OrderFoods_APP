package it.hueic.kenhoang.orderfoods_app.common;

import it.hueic.kenhoang.orderfoods_app.model.User;

/**
 * Created by kenhoang on 26/01/2018.
 */

public class Common {
    public static User currentUser;
    public static String nameProduct = "";

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
}
