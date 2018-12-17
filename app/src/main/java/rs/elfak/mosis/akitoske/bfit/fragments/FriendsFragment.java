package rs.elfak.mosis.akitoske.bfit.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import rs.elfak.mosis.akitoske.bfit.R;
import rs.elfak.mosis.akitoske.bfit.adapters.FriendRecyclerViewAdapter;
import rs.elfak.mosis.akitoske.bfit.models.FriendModel;
import rs.elfak.mosis.akitoske.bfit.models.UserModel;
import rs.elfak.mosis.akitoske.bfit.providers.FirebaseProvider;

public class FriendsFragment extends BaseFragment {

    public static final String FRAGMENT_TAG = "FriendsFragment";

    private RecyclerView mRequestsRecyclerView;
    private RecyclerView mFriendsRecyclerView;
    private ProgressBar mRequestsProgressBar;
    private ProgressBar mFriendsProgressBar;
    private TextView mRequestsEmptyText;
    private TextView mFriendsEmptyText;

    private List<FriendModel> mRequests = new ArrayList<>();
    private List<FriendModel> mFriends = new ArrayList<>();
    private FriendRecyclerViewAdapter mRequestsAdapter;
    private FriendRecyclerViewAdapter mFriendsAdapter;

    private OnListFragmentInteractionListener mListener;

    public interface OnListFragmentInteractionListener {
        void onFriendItemClick(FriendModel item);
        void onFriendRequestAccept(FriendModel item);
        void onFriendRequestDecline(FriendModel item);
    }

    public FriendsFragment() {
        // Required empty public constructor
    }

    @SuppressWarnings("unused")
    public static FriendsFragment newInstance() {
        return new FriendsFragment();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setActionBarTitle("Social");
        getActivity().findViewById(R.id.toolbar_filter_spinner).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        mRequestsRecyclerView = (RecyclerView) view.findViewById(R.id.friend_request_list);
        mRequestsProgressBar = (ProgressBar) view.findViewById(R.id.friend_requests_progress);
        mRequestsEmptyText = (TextView) view.findViewById(R.id.friend_requests_empty);
        mFriendsRecyclerView = (RecyclerView) view.findViewById(R.id.friend_list);
        mFriendsProgressBar = (ProgressBar) view.findViewById(R.id.friends_progress);
        mFriendsEmptyText = (TextView) view.findViewById(R.id.friends_empty);

        Context context = view.getContext();

        // We create and add a simple divider that will be used after every item
        DividerItemDecoration divider = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.list_divider));
        mRequestsRecyclerView.addItemDecoration(divider);
        mFriendsRecyclerView.addItemDecoration(divider);

        mRequestsAdapter = new FriendRecyclerViewAdapter(mRequests, mListener);
        mRequestsRecyclerView.setAdapter(mRequestsAdapter);

        mFriendsAdapter = new FriendRecyclerViewAdapter(mFriends, mListener);
        mFriendsRecyclerView.setAdapter(mFriendsAdapter);

        loadFriendRequestsAndFriends();

        return view;
    }

    private void loadFriendRequestsAndFriends() {
        FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
        String myUserId = firebaseProvider.getCurrentFirebaseUser().getUid();

        firebaseProvider.getUserById(myUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        UserModel user = dataSnapshot.getValue(UserModel.class);
                        user.setId(dataSnapshot.getKey());
                        setupFriendRequests(user.getFriendRequests().keySet());
                        setupFriends(user.getFriends().keySet());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void setupFriendRequests(Set<String> userIds) {
        if (userIds.size() == 0) {
            mRequestsEmptyText.setVisibility(View.VISIBLE);
        } else {
            FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
            for (String userId : userIds) {
                firebaseProvider.getUserById(userId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                FriendModel request = dataSnapshot.getValue(FriendModel.class);
                                request.userId = dataSnapshot.getKey();
                                mRequests.add(request);
                                mRequestsAdapter.notifyItemInserted(mRequests.size() - 1);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }
        }

        mRequestsProgressBar.setVisibility(View.GONE);
    }

    private void setupFriends(Set<String> userIds) {
        if (userIds.size() == 0) {
            mFriendsEmptyText.setVisibility(View.VISIBLE);
        } else {
            FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();
            for (String userId : userIds) {
                firebaseProvider.getUserById(userId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                FriendModel friend = dataSnapshot.getValue(FriendModel.class);
                                friend.userId = dataSnapshot.getKey();
                                mFriends.add(friend);
                                mFriendsAdapter.notifyItemInserted(mFriends.size() - 1);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }
        }

        mFriendsProgressBar.setVisibility(View.GONE);
    }

    public void removeFriendRequest(FriendModel item) {
        int position = mRequests.indexOf(item);
        mRequests.remove(item);
        mRequestsAdapter.notifyItemRemoved(position);
    }

    public void addFriend(FriendModel item) {
        mFriends.add(item);
        mFriendsAdapter.notifyItemInserted(mFriends.size() - 1);

        mFriendsEmptyText.setVisibility(View.GONE);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.setGroupVisible(R.id.main_menu_group, false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRequests.clear();
        mFriends.clear();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}