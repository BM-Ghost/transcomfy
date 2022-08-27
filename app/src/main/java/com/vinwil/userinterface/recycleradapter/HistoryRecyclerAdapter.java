package com.vinwil.userinterface.recycleradapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.vinwil.R;
import com.vinwil.data.model.History;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HistoryRecyclerAdapter extends RecyclerView.Adapter<HistoryRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<History> histories;

    public HistoryRecyclerAdapter(Context context, List<History> histories){
        this.context = context;
        this.histories = histories;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.recycler_adapter_history, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final History history = histories.get(position);
        holder.tvTitle.setText(history.getTitle());
        holder.tvDescription.setText(history.getDescription());
        Date date = new Date(history.getCreatedAt());
        holder.tvCreatedAt.setText(new SimpleDateFormat("EEE dd MMM HH:mm").format(date));
    }

    @Override
    public int getItemCount() {
        return histories.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout rlHistory;
        private TextView tvTitle;
        private TextView tvDescription;
        private TextView tvCreatedAt;

        public ViewHolder(View itemView) {
            super(itemView);
            rlHistory = itemView.findViewById(R.id.rl_history);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvCreatedAt = itemView.findViewById(R.id.tv_created_at);
        }
    }

}
