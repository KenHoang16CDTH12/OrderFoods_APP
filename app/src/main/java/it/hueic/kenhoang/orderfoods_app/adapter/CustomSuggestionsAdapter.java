package it.hueic.kenhoang.orderfoods_app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import java.util.ArrayList;

import it.hueic.kenhoang.orderfoods_app.Interface.ItemClickListener;
import it.hueic.kenhoang.orderfoods_app.R;
import it.hueic.kenhoang.orderfoods_app.common.Common;
import it.hueic.kenhoang.orderfoods_app.model.Product;

/**
 * Created by kenhoang on 29/01/2018.
 */

public class CustomSuggestionsAdapter extends SuggestionsAdapter<Product, CustomSuggestionsAdapter.SuggestionHolder2> {
    public ItemClickListener itemClickListener;
    public class SuggestionHolder2 extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView title;
        public TextView subtitle;
        public ImageView image;

        public SuggestionHolder2(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.suggest_name);
            subtitle = itemView.findViewById(R.id.suggest_price);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onClick(view, getAdapterPosition(), false);
        }
    }


    public CustomSuggestionsAdapter(LayoutInflater inflater, ItemClickListener listener) {
        super(inflater);
        this.itemClickListener = listener;
    }

    @Override
    public void onBindSuggestionHolder(final Product suggestion, SuggestionHolder2 holder, final int position) {
        holder.title.setText(suggestion.getName());
        holder.subtitle.setText("The price is " + suggestion.getPrice() + "$");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClickListener.onClick(view, position, false);
            }
        });
    }

    @Override
    public int getSingleViewHeight() {
        return 80;
    }

    @Override
    public SuggestionHolder2 onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = getLayoutInflater().inflate(R.layout.item_suggest_search, parent, false);
        return new SuggestionHolder2(view);
    }
    /**
     * <b>Override to customize functionality</b>
     * <p>Returns a filter that can be used to constrain data with a filtering
     * pattern.</p>
     * <p>
     * <p>This method is usually implemented by {@link CustomSuggestionsAdapter}
     * classes.</p>
     *
     * @return a filter used to constrain data
     */
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                String term = constraint.toString();
                if(term.isEmpty())
                    suggestions = suggestions_clone;
                else {
                    suggestions = new ArrayList<>();
                    for (Product item: suggestions_clone)
                        if(item.getName().toLowerCase().contains(term.toLowerCase()))
                            suggestions.add(item);
                }
                results.values = suggestions;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                suggestions = (ArrayList<Product>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
