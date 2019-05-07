package de.stephanlindauer.criticalmaps.adapter;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.interfaces.IChatMessage;
import de.stephanlindauer.criticalmaps.utils.TimeToWordStringConverter;
import de.stephanlindauer.criticalmaps.model.chat.ReceivedChatMessage;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ChatMessageViewHolder> {

    private List<IChatMessage> chatMessages;

    public static class ChatMessageViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.firstLine)
        TextView labelView;

        @BindView(R.id.secondLine)
        TextView valueView;

        private final DateFormat dateFormatter = DateFormat.getDateTimeInstance(
                DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault());
        private final Context context;

        public ChatMessageViewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            ButterKnife.bind(this, itemView);
        }

        public void bind(IChatMessage message) {
            valueView.setText(message.getMessage());
            if (message instanceof ReceivedChatMessage) {
                dateFormatter.setTimeZone(TimeZone.getDefault());
                labelView.setText(TimeToWordStringConverter.getTimeAgo(
                        ((ReceivedChatMessage) message).getTimestamp(), context));
                labelView.clearAnimation();
            } else {
                labelView.setText(R.string.chat_sending);

                ObjectAnimator sendingAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(
                        itemView.getContext(),
                        R.animator.map_gps_fab_searching_animation);
                sendingAnimator.setTarget(labelView);
                sendingAnimator.start();
            }
        }
    }

    public ChatMessageAdapter(List<IChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @NonNull
    @Override
    public ChatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View res = inflater.inflate(R.layout.view_chatmessage, parent, false);
        return new ChatMessageViewHolder(res);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageViewHolder holder, int position) {
        holder.bind(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public void updateData(List<IChatMessage> savedAndOutgoingMessages) {
        this.chatMessages = savedAndOutgoingMessages;
        notifyDataSetChanged();
    }
}
