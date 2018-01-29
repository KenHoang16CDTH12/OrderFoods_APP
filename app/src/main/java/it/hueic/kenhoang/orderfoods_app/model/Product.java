package it.hueic.kenhoang.orderfoods_app.model;

/**
 * Created by kenhoang on 29/01/2018.
 */

public class Product {
    private int image;
    private String name;
    private String price;

    public Product() {
    }

    public Product(int image, String name, String price) {
        this.image = image;
        this.name = name;
        this.price = price;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
