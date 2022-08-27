package com.vinwil.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vinwil.R;
import com.vinwil.data.model.Bus;

public class AvailSpaceActivity extends AppCompatActivity {

    private Toolbar tbAvailSpace;
    private TextView tvNumberPlate;
    private LinearLayout llRemove;
    private EditText etAvailSpace;
    private LinearLayout llAdd;

    private Bus bus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avail_space);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        tbAvailSpace = findViewById(R.id.tb_avail_space);
        tvNumberPlate = findViewById(R.id.tv_number_plate);
        llRemove = findViewById(R.id.ll_remove);
        etAvailSpace = findViewById(R.id.et_avail_space);
        llAdd = findViewById(R.id.ll_add);

        setSupportActionBar(tbAvailSpace);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setAvailSpace(); // Set up data and refresh UI

        // Set action in the event remove is clicked
        llRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remove();
            }
        });

        // Set action in the event add is clicked
        llAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_avail_space, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_done:
                done();
                return true;
            default:
                return false;
        }
    }

    private void setAvailSpace() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        final String driverId = auth.getCurrentUser().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("drivers").child(driverId).child("bus")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        bus = dataSnapshot.getValue(Bus.class);
                        bus.setId(dataSnapshot.getKey());
                        bus.setDriverId(driverId);
                        tvNumberPlate.setText(bus.getNumberPlate());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void remove() {
        int space = Integer.parseInt(etAvailSpace.getText().toString());
        int newSpace = space - 1;
        etAvailSpace.setText(String.valueOf(newSpace));
    }

    private void add() {
        int space = Integer.parseInt(etAvailSpace.getText().toString());
        int newSpace = space + 1;
        etAvailSpace.setText(String.valueOf(newSpace));
    }

    private void done() {
        int space = Integer.parseInt(etAvailSpace.getText().toString());
        if(space == 0) {
            Toast.makeText(AvailSpaceActivity.this, "You cannot add zero spaces", Toast.LENGTH_SHORT).show();
        }
        if((bus.getAvailableSpace() + space) > bus.getMaxCapacity()) {
            int maxSpaceToAdd = bus.getMaxCapacity() - bus.getAvailableSpace();
            Toast.makeText(AvailSpaceActivity.this, "Maximum space you can add is  ".concat(String.valueOf(maxSpaceToAdd)), Toast.LENGTH_SHORT).show();
            return;
        }
        int newSpace = bus.getAvailableSpace() + space;
        if(newSpace < 0) {
            newSpace = 0;
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("buses").child(bus.getBusId()).child("availableSpace").setValue(newSpace);
        database.getReference("drivers").child(bus.getDriverId()).child("bus").child("availableSpace").setValue(newSpace);
        if(space > 0) {
            Toast.makeText(AvailSpaceActivity.this, "Added ".concat(String.valueOf(space)).concat(" new spaces"), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(AvailSpaceActivity.this, "Removed ".concat(String.valueOf(space * -1)).concat(" spaces"), Toast.LENGTH_SHORT).show();
        }
        AvailSpaceActivity.this.finish();
    }
}
