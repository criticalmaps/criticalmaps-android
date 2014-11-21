package de.stephanlindauer.criticalmaps.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.helper.TimeToWordStringConverter;
import de.stephanlindauer.criticalmaps.vo.chat.IChatMessage;
import de.stephanlindauer.criticalmaps.vo.chat.OutgoingChatMessage;
import de.stephanlindauer.criticalmaps.vo.chat.ReceivedChatMessage;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

public class ChatMessageAdapter extends ArrayAdapter<IChatMessage> {

    private ArrayList<IChatMessage> chatMessages;
    private Context context;


    public ChatMessageAdapter(Context context, int layoutResourceId, ArrayList<IChatMessage> chatMessages) {
        super(context, layoutResourceId, chatMessages);
        this.chatMessages = chatMessages;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        IChatMessage currentMessage = chatMessages.get(position);

        if (currentMessage instanceof ReceivedChatMessage)
            return buildReceivedMessageView((ReceivedChatMessage) currentMessage, inflater, parent);
        else if (currentMessage instanceof OutgoingChatMessage)
            return buildOutgoingMessageView((OutgoingChatMessage) currentMessage, inflater, parent);

        return null;
    }

    private View buildOutgoingMessageView(OutgoingChatMessage currentMessage, LayoutInflater inflater, ViewGroup parent) {

        View rowView = inflater.inflate(R.layout.outgoing_chatmessage, parent, false);

        TextView labelView = (TextView) rowView.findViewById(R.id.firstLine);
        TextView valueView = (TextView) rowView.findViewById(R.id.secondLine);

        labelView.setText(R.string.sending);
        valueView.setText(currentMessage.getMessage());

        return rowView;
    }

    private View buildReceivedMessageView(ReceivedChatMessage currentMessage, LayoutInflater inflater, ViewGroup parent) {

        View rowView = inflater.inflate(R.layout.chatmessage, parent, false);

        TextView labelView = (TextView) rowView.findViewById(R.id.firstLine);
        TextView valueView = (TextView) rowView.findViewById(R.id.secondLine);

        DateFormat dateFormatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault());
        dateFormatter.setTimeZone(TimeZone.getDefault());

        labelView.setText(TimeToWordStringConverter.getTimeAgo(currentMessage.getTimestamp()));
        valueView.setText(currentMessage.getMessage());

        return rowView;
    }
}