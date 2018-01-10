package com.transcomfy.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.transcomfy.R;
import com.transcomfy.activity.HomeActivity;
import com.transcomfy.data.model.History;
import com.transcomfy.userinterface.recycleradapter.HistoryRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private View rootView;
    private Toolbar tbHistory;
    private RecyclerView rvHistory;

    private List<History> histories;

    public HistoryFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_history, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tbHistory = rootView.findViewById(R.id.tb_history);
        rvHistory = rootView.findViewById(R.id.rv_history);

        ((AppCompatActivity) getContext()).setSupportActionBar(tbHistory);
        ((AppCompatActivity) getContext()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getContext()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        setHasOptionsMenu(true);

        histories = new ArrayList<>();
        rvHistory.setNestedScrollingEnabled(false);
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHistory.setAdapter(new HistoryRecyclerAdapter(getContext(), histories));

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users").child(auth.getUid()).child("history")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        histories.clear();
                        rvHistory.getAdapter().notifyDataSetChanged();
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            History history = snapshot.getValue(History.class);
                            histories.add(history);
                            rvHistory.getAdapter().notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                ((HomeActivity) getContext()).getDlHome().openDrawer(GravityCompat.START, true);
                return true;
            default:
                return false;
        }
    }

}
