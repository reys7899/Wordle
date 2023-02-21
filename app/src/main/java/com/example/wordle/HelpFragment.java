package com.example.wordle;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 *
 * @author: Rey Sanayei
 * @description: The Help Fragment's java code. Responsible for displaying the help page.
 *
 */

public class HelpFragment extends androidx.fragment.app.DialogFragment {

    public Activity containerActivity = null;
    private ImageView closeBtn;

    /**
     * Required empty public constructor
     */
    public HelpFragment() {
    }

    /**
     * Sets the container Activity for this fragment.
     *
     * @param containerActivity The container Activity for this fragment.
     */
    public void setContainerActivity(Activity containerActivity) {
        this.containerActivity = containerActivity;
    }

    /**
     * The method called on the creation of the fragment's instance. This method also defines
     * the OnClick event-handling for the readOn Button.
     *
     * @param inflater           The LayoutInflater
     * @param container          The ViewGroup that contains this fragment
     * @param savedInstanceState The Bundle that holds saved information for recovering this instance
     * @return View The view that gets inflated is returned.
     */
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_help, container, false);
        closeBtn = v.findViewById(R.id.close_button);
        closeBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dismissAllowingStateLoss();

            }
        });
        return v;
    }
}