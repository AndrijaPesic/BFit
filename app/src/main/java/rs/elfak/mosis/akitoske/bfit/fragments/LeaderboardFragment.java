package rs.elfak.mosis.akitoske.bfit.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rs.elfak.mosis.akitoske.bfit.R;
import rs.elfak.mosis.akitoske.bfit.adapters.RankRecyclerViewAdapter;
import rs.elfak.mosis.akitoske.bfit.models.UserModel;
import rs.elfak.mosis.akitoske.bfit.providers.FirebaseProvider;

public class LeaderboardFragment extends BaseFragment {

    public static final String FRAGMENT_TAG = "LeaderboardFragment";

    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_USER_FRIENDS = "user_friends";

    private Context mContext;

    private String mUserId;
    private Map<String, Boolean> mUserFriends;

    private TextView mUserCountTv;
    private RecyclerView mRanksRecyclerView;
    private ProgressBar mRanksProgressBar;

    private List<UserModel> mUsers = new ArrayList<>();
    private RankRecyclerViewAdapter mRankAdapter;

    private OnListFragmentInteractionListener mListener;

    public interface OnListFragmentInteractionListener {
        void onLeaderboardItemClick(String userId);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setActionBarTitle("Leaderboard");
        getActivity().findViewById(R.id.toolbar_filter_spinner).setVisibility(View.INVISIBLE);
    }

    public LeaderboardFragment() {
        // Required empty public constructor
    }

    public static LeaderboardFragment newInstance(String userId, Map<String, Boolean> userFriends) {
        LeaderboardFragment fragment = new LeaderboardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        args.putSerializable(ARG_USER_FRIENDS, (HashMap<String, Boolean>) userFriends);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserId = getArguments().getString(ARG_USER_ID);
            mUserFriends = (HashMap<String, Boolean>) getArguments().getSerializable(ARG_USER_FRIENDS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        mUserCountTv = view.findViewById(R.id.leaderboard_user_count);
        mRanksRecyclerView = view.findViewById(R.id.leaderboard_list);
        mRanksProgressBar = view.findViewById(R.id.leaderboard_progress);

        Context context = view.getContext();

        // We create and add a simple divider that will be used after every item
        DividerItemDecoration divider = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.list_divider));
        mRanksRecyclerView.addItemDecoration(divider);

        mRankAdapter = new RankRecyclerViewAdapter(
                FirebaseProvider.getInstance().getCurrentFirebaseUser().getUid(), mUsers, mListener);
        mRanksRecyclerView.setAdapter(mRankAdapter);

        loadLeaderboard();

        return view;
    }

    private void loadLeaderboard() {
        FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();

        firebaseProvider.getAllUsers().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUserCountTv.setText(String.valueOf(dataSnapshot.getChildrenCount()) + " players");

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    UserModel user = userSnapshot.getValue(UserModel.class);
                    user.setId(userSnapshot.getKey());
                    mUsers.add(user);
                }

                Collections.sort(mUsers, new Comparator<UserModel>() {
                    @Override
                    public int compare(UserModel o1, UserModel o2) {
                        return o2.getPoints() - o1.getPoints();
                    }
                });

                mRankAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRanksProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUsers.clear();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mContext = null;
    }

}