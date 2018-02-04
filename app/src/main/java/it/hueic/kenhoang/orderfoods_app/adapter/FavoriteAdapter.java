package it.hueic.kenhoang.orderfoods_app.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import it.hueic.kenhoang.orderfoods_app.FoodDetailActivity;
import it.hueic.kenhoang.orderfoods_app.Interface.ItemClickListener;
import it.hueic.kenhoang.orderfoods_app.R;
import it.hueic.kenhoang.orderfoods_app.adapter.ViewHolder.FoodVIewHolder;
import it.hueic.kenhoang.orderfoods_app.common.Common;
import it.hueic.kenhoang.orderfoods_app.database.Database;
import it.hueic.kenhoang.orderfoods_app.model.Food;

/**
 * Created by kenhoang on 04/02/2018.
 */

public class FavoriteAdapter extends RecyclerView.Adapter<FoodVIewHolder> {
    private List<String> listData = new ArrayList<>();
    private Activity context;

    public FavoriteAdapter(List<String> listData, Activity context) {
        this.listData = listData;
        this.context = context;
    }

    @Override
    public FoodVIewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_food_ref_menu, parent, false);
        return new FoodVIewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final FoodVIewHolder holder, final int position) {
        final Database localDB = new Database(context);
        DatabaseReference foodData = FirebaseDatabase.getInstance().getReference("Foods");
        foodData.orderByKey().equalTo(listData.get(position))
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    final Food item = snapshot.getValue(Food.class);
                    Picasso.with(context)
                            .load(item.getImage())
                            .into(holder.imgFood);
                    holder.tvFoodName.setText(item.getName());
                    //Add Favorites
                    if (localDB.isFavorite(listData.get(position)))
                        holder.imgFav.setImageResource(R.drawable.ic_favorite_black_24dp);

                    holder.imgFav.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!localDB.isFavorite(listData.get(position))) {
                                localDB.addToFavorites(listData.get(position));
                                holder.imgFav.setImageResource(R.drawable.ic_favorite_black_24dp);
                                Common.showSnackBar("" + item.getName() + " was added to Favorites", context, context.findViewById(R.id.listFavoritefoodMain));
                            } else {
                                localDB.removeFromFavorites(listData.get(position));
                                listData.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, listData.size());
                                holder.imgFav.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                                Common.showSnackBar("" + item.getName() + " was remove from to Favorites", context, context.findViewById(R.id.listFavoritefoodMain));
                            }
                        }
                    });
                    holder.setItemClickListener(new ItemClickListener() {
                        @Override
                        public void onClick(View view, int position, boolean isLongClick) {
                            //Start new Activity
                            Intent foodDetailIntent = new Intent(context, FoodDetailActivity.class);
                            foodDetailIntent.putExtra("FoodId", listData.get(position)); //Send Food Id to new activity
                            context.startActivity(foodDetailIntent);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    public int getItemCount() {
        return listData.size();
    }
}