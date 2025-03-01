package com.example.ctrlaltelite;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

public class DeleteFragment extends Fragment {

    // Initialization parameters (to be renamed later)
    private static final String PARAM1 = "param1";
    private static final String PARAM2 = "param2";

    // Empty Constructor
    public DeleteFragment() {
    }

    /**
     * Create new instance of DeleteFragment using given arguments (to be renamed)
     * @param arg1
     * @param arg2
     * @return new instance of DeleteFragment
     */
    public static DeleteFragment newInstance (String arg1, String arg2) {
        Bundle args = new Bundle();
        DeleteFragment fragment = new DeleteFragment();

        // Storing the parameters in the bundle
        args.putString(PARAM1, arg1);
        args.putString(PARAM2, arg2);

        // Setting the bundle of arguments to our fragment
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment to be displayed to the school
        return inflater.inflate(R.layout.fragment_editdelete, container, false);
    }
}