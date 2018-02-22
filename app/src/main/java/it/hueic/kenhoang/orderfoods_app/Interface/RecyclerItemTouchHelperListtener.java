package it.hueic.kenhoang.orderfoods_app.Interface;

import android.support.v7.widget.RecyclerView;

/**
 * Created by kenhoang on 22/02/2018.
 */

public interface RecyclerItemTouchHelperListtener {
    void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
}
