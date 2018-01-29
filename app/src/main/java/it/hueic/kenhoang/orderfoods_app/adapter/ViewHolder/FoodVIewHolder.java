package it.hueic.kenhoang.orderfoods_app.adapter.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.flaviofaria.kenburnsview.KenBurnsView;

import it.hueic.kenhoang.orderfoods_app.Interface.ItemClickListener;
import it.hueic.kenhoang.orderfoods_app.R;

/**
 * Created by kenhoang on 27/01/2018.
 */

public class FoodVIewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView tvFoodName;
    public KenBurnsView imgFood;
    private ItemClickListener itemClickListener;
    public FoodVIewHolder(View itemView) {
        super(itemView);
        tvFoodName  = itemView.findViewById(R.id.food_name);
        imgFood  = itemView.findViewById(R.id.food_image);
        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(), false);
    }
}