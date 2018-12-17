package rs.elfak.mosis.akitoske.bfit.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

import rs.elfak.mosis.akitoske.bfit.R;
import rs.elfak.mosis.akitoske.bfit.activities.ProfileActivity;
import rs.elfak.mosis.akitoske.bfit.fragments.FriendsFragment;
import rs.elfak.mosis.akitoske.bfit.models.FriendModel;
import rs.elfak.mosis.akitoske.bfit.providers.FirebaseProvider;

public class FriendRecyclerViewAdapter extends
        RecyclerView.Adapter<FriendRecyclerViewAdapter.FriendViewHolder>{

    private final List<FriendModel> mItems;
    private final FriendsFragment.OnListFragmentInteractionListener mListener;

    public FriendRecyclerViewAdapter(List<FriendModel> items, FriendsFragment.OnListFragmentInteractionListener listener) {
        mItems = items;
        mListener = listener;
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_list_item, parent, false);
        return new FriendViewHolder(view, parent);
    }

    @Override
    public void onBindViewHolder(final FriendViewHolder holder, int position) {
        holder.mItem = mItems.get(position);
        holder.mFullName.setText(holder.mItem.fullName);
        holder.mDisplayName.setText(holder.mItem.displayName);

        // We need to get context somehow to be able to use Glide here
        Context context = holder.mAvatarImg.getContext();
        Glide.with(context)
                .load(holder.mItem.avatarUrl)
                .into(holder.mAvatarImg);

        holder.mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onFriendItemClick(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final View mItemView;
        public final TextView mFullName;
        public final TextView mDisplayName;
        public final ImageView mAvatarImg;
        public FriendModel mItem;

        public FriendViewHolder(View itemView, ViewGroup parent) {
            super(itemView);
            mItemView = itemView;

            mFullName = itemView.findViewById(R.id.friend_request_item_full_name);
            mDisplayName = itemView.findViewById(R.id.friend_request_item_display_name);
            mAvatarImg = itemView.findViewById(R.id.friend_request_item_avatar_img);
            Button acceptButton = itemView.findViewById(R.id.friend_request_item_accept_btn);
            Button declineButton = itemView.findViewById(R.id.friend_request_item_decline_btn);

            // If it's a "friendRequest" item, we want to use the Accept/Decline buttons
            // that are invisible for the "friend" items
            if (parent.getId() == R.id.friend_request_list) {
                acceptButton.setOnClickListener(this);
                acceptButton.setVisibility(View.VISIBLE);
                declineButton.setOnClickListener(this);
                declineButton.setVisibility(View.VISIBLE);
            }
            mItemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int viewId = v.getId();
            FriendModel friendItem = mItems.get(getAdapterPosition());

            if (viewId == mItemView.getId()) {
                mListener.onFriendItemClick(friendItem);
            } else if (viewId == R.id.friend_request_item_accept_btn) {
                mListener.onFriendRequestAccept(friendItem);
            } else if (viewId == R.id.friend_request_item_decline_btn) {
                mListener.onFriendRequestDecline(friendItem);
            }
        }
    }
}
