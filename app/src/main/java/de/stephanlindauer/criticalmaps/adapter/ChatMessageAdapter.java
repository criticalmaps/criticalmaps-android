package de.stephanlindauer.criticalmaps.adapter;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;

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

    static class ChatMessageViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.firstLine)
        TextView labelView;

        @BindView(R.id.secondLine)
        TextView valueView;

        private final DateFormat dateFormatter = DateFormat.getDateTimeInstance(
                DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault());
        private ObjectAnimator sendingAnimator;

        ChatMessageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(IChatMessage message) {
            valueView.setText(message.getMessage());
            if (message instanceof ReceivedChatMessage) {
                dateFormatter.setTimeZone(TimeZone.getDefault());
                labelView.setText(TimeToWordStringConverter.getTimeAgo(
                        ((ReceivedChatMessage) message).getTimestamp(), itemView.getContext()));
            } else {
                labelView.setText(R.string.chat_sending);
                
                sendingAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(
                        itemView.getContext(), R.animator.map_gps_fab_searching_animation);
                sendingAnimator.setTarget(labelView);
                sendingAnimator.start();
            }
        }

        void clearAnimation() {
            if (sendingAnimator != null) {
                sendingAnimator.cancel();
                labelView.setAlpha(1f);
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
    public void onViewDetachedFromWindow(@NonNull ChatMessageViewHolder holder) {
        holder.clearAnimation();
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
