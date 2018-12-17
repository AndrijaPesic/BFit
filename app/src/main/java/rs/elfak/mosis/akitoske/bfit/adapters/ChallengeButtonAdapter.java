package rs.elfak.mosis.akitoske.bfit.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import rs.elfak.mosis.akitoske.bfit.R;
import rs.elfak.mosis.akitoske.bfit.models.ChallengeType;

public class ChallengeButtonAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<ChallengeType> mItems = new ArrayList<>();
    private final OnAddChallengeItemClickListener mListener;

    public interface OnAddChallengeItemClickListener {
        void onAddChallengeClick(ChallengeType structureType);
    }

    public ChallengeButtonAdapter(Context context, OnAddChallengeItemClickListener listener) {
        mContext = context;
        mListener = listener;
        // get all types and add them to the array
        mItems.addAll(Arrays.asList(ChallengeType.values()));
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChallengeButtonViewHolder holder;
        View view = convertView;

        // If the view is being created, inflate the layout and attach a new holder as a tag
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.grid_item, parent, false);
            holder = new ChallengeButtonViewHolder(view, parent);
            view.setTag(holder);
        } else {
            // If the view exists and is being recycled, just get the holder from the tag
            holder = (ChallengeButtonViewHolder) view.getTag();
        }

        // Set the appropriate item
        holder.mItem = mItems.get(position);

        holder.mImageButton.setImageResource(holder.mItem.getIconResId());
        holder.mName.setText(holder.mItem.getName());
        holder.mCost.setText(String.valueOf(holder.mItem.getBaseCost()));

        return view;
    }

    // View holder class for the item
    private class ChallengeButtonViewHolder implements View.OnClickListener {
        private ChallengeType mItem;
        private ImageButton mImageButton;
        private TextView mName;
        private TextView mCost;

        private ChallengeButtonViewHolder(View view, ViewGroup parent) {
            mImageButton = view.findViewById(R.id.grid_item_image_btn);
            mName = view.findViewById(R.id.grid_item_name_text);
            mCost = view.findViewById(R.id.grid_item_cost_text);

            mImageButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onAddChallengeClick(mItem);
        }
    }
}