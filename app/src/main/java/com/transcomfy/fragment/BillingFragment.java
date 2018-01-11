package com.transcomfy.fragment;

import android.content.Intent;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.transcomfy.R;
import com.transcomfy.activity.HomeActivity;
import com.transcomfy.activity.TopUpAccountActivity;
import com.transcomfy.data.Keys;
import com.transcomfy.data.model.Payment;
import com.transcomfy.userinterface.recycleradapter.PaymentHistoryRecyclerAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BillingFragment extends Fragment {

    private View rootView;
    private Toolbar tbBilling;
    private TextView tvBalance;
    private RecyclerView rvTopUpHistory;
    private Button btnTopUpAccount;

    private List<Payment> payments;
    private double balance = -1;

    public BillingFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_billing, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tbBilling = rootView.findViewById(R.id.tb_billing);
        tvBalance = rootView.findViewById(R.id.tv_balance);
        rvTopUpHistory = rootView.findViewById(R.id.rv_top_up_history);
        btnTopUpAccount = rootView.findViewById(R.id.btn_top_up_account);

        ((AppCompatActivity) getContext()).setSupportActionBar(tbBilling);
        ((AppCompatActivity) getContext()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getContext()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((AppCompatActivity) getContext()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_18dp);
        setHasOptionsMenu(true);

        payments = new ArrayList<>();
        rvTopUpHistory.setNestedScrollingEnabled(false);
        rvTopUpHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTopUpHistory.setAdapter(new PaymentHistoryRecyclerAdapter(getContext(), payments));

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users").child(auth.getUid()).child("billing")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        balance = dataSnapshot.child("balance").getValue(Double.class);
                        tvBalance.setText("KSH " + balance);

                        payments.clear();
                        rvTopUpHistory.getAdapter().notifyDataSetChanged();
                        DataSnapshot snapshot = dataSnapshot.child("paymentHistory");
                        for(DataSnapshot snap : snapshot.getChildren()) {
                            if(snap.child("createdAt").getValue(Long.class) != null
                                    && snap.child("amount").getValue(Double.class) != null) {
                                long createdAt = snap.child("createdAt").getValue(Long.class);
                                double amount = snap.child("amount").getValue(Double.class);

                                Payment payment = new Payment();
                                Date date = new Date(createdAt);
                                payment.setCreatedAt(new SimpleDateFormat("EEE dd MMM yyyy HH:mm").format(date));
                                payment.setAmount(String.valueOf(amount));
                                payments.add(payment);
                                rvTopUpHistory.getAdapter().notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        btnTopUpAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(balance == -1) {
                    Toast.makeText(getContext(), "Not ready", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(getContext(), TopUpAccountActivity.class);
                intent.putExtra(Keys.EXTRA_BALANCE, balance);
                startActivity(intent);
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
