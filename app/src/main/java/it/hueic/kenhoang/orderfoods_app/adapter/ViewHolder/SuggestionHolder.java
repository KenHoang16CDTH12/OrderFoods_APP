package it.hueic.kenhoang.orderfoods_app.adapter.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import it.hueic.kenhoang.orderfoods_app.Interface.ItemClickListener;
import it.hueic.kenhoang.orderfoods_app.R;

/**
 * Created by kenhoang on 29/01/2018.
 */

public class SuggestionHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public TextView title;
    public TextView subtitle;
    public ImageView image;
    public ItemClickListener itemClickListener;
    public SuggestionHolder(View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.suggest_name);
        subtitle = itemView.findViewById(R.id.suggest_price);
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
