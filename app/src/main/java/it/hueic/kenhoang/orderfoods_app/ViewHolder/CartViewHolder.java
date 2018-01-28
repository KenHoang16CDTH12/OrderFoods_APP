package it.hueic.kenhoang.orderfoods_app.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import it.hueic.kenhoang.orderfoods_app.Interface.ItemClickListener;
import it.hueic.kenhoang.orderfoods_app.R;

/**
 * Created by kenhoang on 28/01/2018.
 */

public class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public TextView txt_card_name, txt_price;
    public ImageView img_cart_count;
    public ItemClickListener itemClickListener;
    public CartViewHolder(View itemView) {
        super(itemView);
        txt_card_name = itemView.findViewById(R.id.cart_item_name);
        txt_price = itemView.findViewById(R.id.cart_item_price);
        img_cart_count = itemView.findViewById(R.id.cart_item_count);
        itemView.setOnClickListener(this);

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {

    }
}
