package com.vinwil.userinterface.recycleradapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.vinwil.R;
import com.vinwil.data.Keys;
import com.vinwil.data.model.Stop;

import java.util.List;

public class SearchDestinationRecyclerAdapter extends RecyclerView.Adapter<SearchDestinationRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<Stop> stops;

    public SearchDestinationRecyclerAdapter(Context context, List<Stop> stops){
        this.context = context;
        this.stops = stops;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.recycler_adapter_search_destination, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Stop stop = stops.get(position);
        holder.tvName.setText(stop.getName());

        holder.rlSearchDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra(Keys.EXTRA_STOP, stop);
                ((AppCompatActivity) context).setResult(AppCompatActivity.RESULT_OK, intent);
                ((AppCompatActivity) context).finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return stops.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout rlSearchDestination;
        private TextView tvName;

        public ViewHolder(View itemView) {
            super(itemView);
            rlSearchDestination = itemView.findViewById(R.id.rl_search_destination);
            tvName = itemView.findViewById(R.id.tv_name);
        }
    }

}
