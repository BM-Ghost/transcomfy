package com.transcomfy.userinterface.recycleradapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.transcomfy.R;
import com.transcomfy.data.model.Payment;
import com.transcomfy.data.model.Request;

import java.util.List;

public class PaymentHistoryRecyclerAdapter extends RecyclerView.Adapter<PaymentHistoryRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<Payment> payments;

    public PaymentHistoryRecyclerAdapter(Context context, List<Payment> payments){
        this.context = context;
        this.payments = payments;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.recycler_adapter_payment_history, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Payment payment = payments.get(position);
        holder.tvCreatedAt.setText(payment.getCreatedAt());
        holder.tvAmount.setText("KSH ".concat(payment.getAmount()));
    }

    @Override
    public int getItemCount() {
        return payments.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout rlPaymentHistory;
        private TextView tvCreatedAt;
        private TextView tvAmount;

        public ViewHolder(View itemView) {
            super(itemView);
            rlPaymentHistory = itemView.findViewById(R.id.rl_payment_history);
            tvCreatedAt = itemView.findViewById(R.id.tv_created_at);
            tvAmount = itemView.findViewById(R.id.tv_amount);
        }
    }

}
