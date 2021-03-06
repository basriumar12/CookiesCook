package com.ray.cookiescook.adapter;

import android.content.Context;
import android.database.Cursor;
import android.os.Debug;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ray.cookiescook.R;
import com.ray.cookiescook.database.StepsColumns;
import com.squareup.picasso.Picasso;

import net.simonvt.schematic.Cursors;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Ray on 9/8/2017.
 */

public class StepListAdapter extends RecyclerView.Adapter<StepListAdapter.ViewHolder> {


    private int mSelectedPosition = -1;
    private static final String TAG = "StepListAdapter";

    public interface RecyclerClickListener {
        void onItemClickListener(int position);
    }

    StepListAdapter.RecyclerClickListener mClickListener;
    private Cursor mCursor;
    private Context mContext;

    public StepListAdapter(StepListAdapter.RecyclerClickListener listener) {
        mClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        Debug.stopMethodTracing();
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position == 0) {
            Picasso.with(mContext).load(R.drawable.step_placeholder).fit().centerCrop().into(holder.imageThumb);
            holder.textDescription.setText("Ingredients");
        } else {
            int newPos = position - 1;
            mCursor.moveToPosition(newPos);
            String strStepDesc = Cursors.getString(mCursor, StepsColumns.SHORT_DESCRIPTION);
            String thumbUrl = Cursors.getString(mCursor, StepsColumns.THUMBNAIL_URL);
            if (thumbUrl.isEmpty()) {
                Picasso.with(mContext).load(R.drawable.step_placeholder).fit().centerCrop().into(holder.imageThumb);
            } else {
                Picasso.with(mContext).load(thumbUrl).placeholder(R.drawable.step_placeholder).fit().centerCrop().into(holder.imageThumb);
            }
            holder.textDescription.setText(strStepDesc);
        }
        try {
            if (position == mSelectedPosition) {
                holder.cardStep.setSelected(true);
            } else {
                holder.cardStep.setSelected(false);
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "onBindViewHolder: " + e.toString());
        }
    }

    public void setCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) return 0;
        else return mCursor.getCount() + 1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.image_thumb)
        ImageView imageThumb;
        @BindView(R.id.text_step_description)
        TextView textDescription;
        @BindView(R.id.card_step)
        CardView cardStep;
        @BindView(R.id.container)
        LinearLayout container;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            container.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mSelectedPosition = getAdapterPosition();
            cardStep.setSelected(true);
            mClickListener.onItemClickListener(getAdapterPosition());
            notifyDataSetChanged();
        }
    }
}
