package rs.elfak.mosis.akitoske.bfit.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import rs.elfak.mosis.akitoske.bfit.R;

public class NoLocationFragment extends BaseFragment implements View.OnClickListener {

    public static final String FRAGMENT_TAG = "NoLocationFragment";

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {
        void onNoLocationContinueClick();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setActionBarTitle(null);
        getActivity().findViewById(R.id.toolbar_filter_spinner).setVisibility(View.INVISIBLE);
    }

    public NoLocationFragment() {
        // Required empty public constructor
    }

    public static NoLocationFragment newInstance() {
        NoLocationFragment fragment = new NoLocationFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_no_location, container, false);

        Button continueButton = (Button) view.findViewById(R.id.no_location_continue_btn);
        continueButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            mListener.onNoLocationContinueClick();
        }
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
