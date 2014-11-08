package de.stephanlindauer.criticalmass.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.stephanlindauer.criticalmass.R;
import de.stephanlindauer.criticalmass.helper.TimeToWordStringConverter;
import de.stephanlindauer.criticalmass.vo.ChatMessage;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

public class ChatMessageAdapter extends ArrayAdapter<ChatMessage> {
    private Context context;

    private ArrayList<ChatMessage> chatMessages;

    int layoutResourceId;
    public ChatMessageAdapter(Context context, int layoutResourceId, ArrayList<ChatMessage> chatMessages) {
        super(context, layoutResourceId, chatMessages );
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.chatMessages = chatMessages;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.chatmessage, parent, false);

        TextView labelView = (TextView) rowView.findViewById(R.id.firstLine);
        TextView valueView = (TextView) rowView.findViewById(R.id.secondLine);

        DateFormat dateFormatter = DateFormat.getDateTimeInstance( DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault() );
        dateFormatter.setTimeZone(TimeZone.getDefault());

        labelView.setText(TimeToWordStringConverter.getTimeAgo(chatMessages.get(position).getTimestamp()));
        valueView.setText(chatMessages.get(position).getMessage());

        return rowView;
    }


    public ArrayList<ChatMessage> getChatMessages() {
        return chatMessages;
    }
}