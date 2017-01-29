package ru.velkonost.lume.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import ru.velkonost.lume.R;

public class AddColumnFragment extends AbstractTabFragment {
    private static final int LAYOUT = R.layout.fragment_add_column;

    public static AddColumnFragment getInstance(Context context) {
        Bundle args = new Bundle();
        AddColumnFragment fragment = new AddColumnFragment();

        fragment.setArguments(args);
        fragment.setContext(context);
        fragment.setTitle("+");

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(LAYOUT, container, false);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



            }
        });

        return view;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
