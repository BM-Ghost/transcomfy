package com.vinwil.userinterface.recycleradapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.vinwil.R;
import com.vinwil.data.model.Request;

import java.util.List;

public class PendingRequestsRecyclerAdapter extends RecyclerView.Adapter<PendingRequestsRecyclerAdapter.ViewHolder> {

    public interface OnItemClicked {
        void onItemClicked(Request request);
    }

    private Context context;
    private List<Request> requests;

    private OnItemClicked onItemClicked;

    public PendingRequestsRecyclerAdapter(Context context, List<Request> requests){
        this.context = context;
        this.requests = requests;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.recycler_adapter_pending_requests, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Request request = requests.get(position);
        holder.tvName.setText(request.getName());
        holder.tvStage.setText(request.getLocation().getName());
        holder.tvStatus.setText(request.getStatus());

        holder.rlPendingRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClicked != null) {
                    onItemClicked.onItemClicked(request);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public void setOnItemClicked(OnItemClicked onItemClicked) {
        this.onItemClicked = onItemClicked;
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout rlPendingRequests;
        private TextView tvName;
        private TextView tvStage;
        private TextView tvStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            rlPendingRequests = itemView.findViewById(R.id.rl_pending_requests);
            tvName = itemView.findViewById(R.id.tv_name);
            tvStage = itemView.findViewById(R.id.tv_stage);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }
    }

}
