package de.stephanlindauer.criticalmaps.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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

    private final List<IChatMessage> chatMessages;

    public ChatMessageAdapter(List<IChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public class ChatMessageViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.firstLine)
        TextView labelView;

        @BindView(R.id.secondLine)
        TextView valueView;

        private final DateFormat dateFormatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault());
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
                labelView.setText(TimeToWordStringConverter.getTimeAgo(((ReceivedChatMessage) message).getTimestamp(), context));
            } else {
                labelView.setText(R.string.chat_sending);
            }
        }
    }

    @Override
    public ChatMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View res;
        if (viewType == 0) {
            res = inflater.inflate(R.layout.view_chatmessage, parent, false);
        } else {
            res = inflater.inflate(R.layout.view_outgoing_chatmessage, parent, false);
        }
        return new ChatMessageViewHolder(res);
    }

    @Override
    public void onBindViewHolder(ChatMessageViewHolder holder, int position) {
        holder.bind(chatMessages.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position) instanceof ReceivedChatMessage) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }
}