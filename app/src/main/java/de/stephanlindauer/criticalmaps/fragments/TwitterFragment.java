package de.stephanlindauer.criticalmaps.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.adapter.ChatMessageAdapter;
import de.stephanlindauer.criticalmaps.handler.TwitterGetHandler;
import de.stephanlindauer.criticalmaps.vo.chat.OutgoingChatMessage;

public class TwitterFragment extends SuperFragment {

    //view
    private View twitterView;

    //adapter
    private ChatMessageAdapter chatMessageAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        twitterView = inflater.inflate(R.layout.fragment_twitter, container, false);
        return twitterView;
    }

    @Override
    public void onActivityCreated(final Bundle savedState) {
        super.onActivityCreated(savedState);
        new TwitterGetHandler().execute();

        super.onActivityCreated(savedState);

        chatMessageAdapter = new ChatMessageAdapter(getActivity(), R.layout.view_chatmessage, chatModel.getSavedAndOutgoingMessages());

        chatListView = (ListView) getActivity().findViewById(R.id.chat_list);
        chatListView.setAdapter(chatMessageAdapter);

        chatMessageAdapter.notifyDataSetChanged();

        chatListView.setSelection(chatListView.getCount());

        chatListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        isScrolling = true;
                        return false;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        isScrolling = false;
                        return false;
                }
                return false;
            }
        });

        editMessageTextfield = (EditText) getActivity().findViewById(R.id.chat_edit_message);

        Button sendButton = (Button) getActivity().findViewById(R.id.chat_send_btn);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editMessageTextfield.getText().toString();

                if (message.equals(""))
                    return;

                chatModel.setNewOutgoingMessage(new OutgoingChatMessage(message));

                editMessageTextfield.setText("");
                chatMessageAdapter = new ChatMessageAdapter(getActivity(), 123, chatModel.getSavedAndOutgoingMessages());
                chatListView.setAdapter(chatMessageAdapter);

                if (!isScrolling)
                    chatListView.setSelection(chatListView.getCount());
            }
        });

    }
}