package rs.elfak.mosis.akitoske.bfit.fragments;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;


public abstract class BaseFragment extends Fragment {

    public void setActionBarTitle(String newTitle) {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            if (newTitle != null) {
                actionBar.setTitle(newTitle);
                actionBar.setDisplayShowTitleEnabled(true);
            } else {
                actionBar.setDisplayShowTitleEnabled(false);
            }
        }
    }

}
