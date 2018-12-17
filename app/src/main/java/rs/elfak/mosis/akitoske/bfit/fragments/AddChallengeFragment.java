package rs.elfak.mosis.akitoske.bfit.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import rs.elfak.mosis.akitoske.bfit.R;
import rs.elfak.mosis.akitoske.bfit.adapters.ChallengeButtonAdapter;
import rs.elfak.mosis.akitoske.bfit.models.ChallengeType;

public class AddChallengeFragment extends BaseFragment implements ChallengeButtonAdapter.OnAddChallengeItemClickListener{

    public static final String FRAGMENT_TAG = "AddChallengeFragment";

    private Context mContext;

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {
        void onAddChallengeClick(ChallengeType challengeType);
    }

    public AddChallengeFragment() {
        // Required empty public constructor
    }

    public static AddChallengeFragment newInstance() {
        return new AddChallengeFragment();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setActionBarTitle(getString(R.string.add_challenge_title));
        getActivity().findViewById(R.id.toolbar_filter_spinner).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_challenge, container, false);

        GridView gridView = view.findViewById(R.id.fragment_add_challenge_grid);
        gridView.setAdapter(new ChallengeButtonAdapter(mContext, this));

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.setGroupVisible(R.id.main_menu_group, false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

        setHasOptionsMenu(true);
        mContext = context;
    }

    @Override
    public void onAddChallengeClick(ChallengeType challengeType) {
        mListener.onAddChallengeClick(challengeType);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mContext = null;
    }

}
