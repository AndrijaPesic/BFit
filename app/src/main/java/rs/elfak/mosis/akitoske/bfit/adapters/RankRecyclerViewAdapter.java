package rs.elfak.mosis.akitoske.bfit.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import rs.elfak.mosis.akitoske.bfit.R;
import rs.elfak.mosis.akitoske.bfit.fragments.LeaderboardFragment;
import rs.elfak.mosis.akitoske.bfit.models.UserModel;

public class RankRecyclerViewAdapter extends RecyclerView.Adapter<RankRecyclerViewAdapter.RankViewHolder> {

    private String mLoggedUserId;
    private final List<UserModel> mItems;
    private final LeaderboardFragment.OnListFragmentInteractionListener mListener;

    public RankRecyclerViewAdapter(String loggedUserId, List<UserModel> items,
                                   LeaderboardFragment.OnListFragmentInteractionListener listener) {
        mLoggedUserId = loggedUserId;
        mItems = items;
        mListener = listener;
    }

    @Override
    public RankRecyclerViewAdapter.RankViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.leaderboard_list_item, parent, false);
        return new RankViewHolder(view, parent);
    }

    @Override
    public void onBindViewHolder(RankViewHolder holder, int position) {
        holder.mItem = mItems.get(position);
        holder.mRank.setText(String.valueOf(position + 1));
        holder.mDisplayName.setText(holder.mItem.getDisplayName());
        holder.mPoints.setText(String.valueOf(holder.mItem.getPoints()));

        // We need to get context somehow to be able to use Glide here
        Context context = holder.mAvatarImg.getContext();
        Glide.with(context)
                .load(holder.mItem.getAvatarUrl())
                .into(holder.mAvatarImg);

        if (holder.mItem.getFriends().containsKey(mLoggedUserId)) {
            holder.mFriendImg.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class RankViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final View mItemView;
        public final TextView mRank;
        public final ImageView mAvatarImg;
        public final TextView mDisplayName;
        public final TextView mPoints;
        public final ImageView mFriendImg;
        public UserModel mItem;

        public RankViewHolder(View itemView, ViewGroup parent) {
            super(itemView);
            mItemView = itemView;

            mRank = itemView.findViewById(R.id.leaderboard_item_rank);
            mAvatarImg = itemView.findViewById(R.id.leaderboard_item_avatar);
            mDisplayName = itemView.findViewById(R.id.leaderboard_item_display_name);
            mPoints = itemView.findViewById(R.id.leaderboard_item_points);
            mFriendImg = itemView.findViewById(R.id.leaderboard_item_friend);

            mItemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String clickedUserId = mItems.get(getAdapterPosition()).getId();
            mListener.onLeaderboardItemClick(clickedUserId);
        }
    }
}