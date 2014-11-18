package de.stephanlindauer.criticalmass.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import de.stephanlindauer.criticalmass.R;
import de.stephanlindauer.criticalmass.adapter.ChatMessageAdapter;
import de.stephanlindauer.criticalmass.events.NewServerResponseEvent;
import de.stephanlindauer.criticalmass.model.ChatModel;
import de.stephanlindauer.criticalmass.service.ServerPuller;

public class ChatFragment extends SuperFragment {

    //dependencies
    private final ChatModel chatModel = ChatModel.getInstance();
    private final ServerPuller serverPuller = ServerPuller.getInstance();

    //view
    private View chatView;
    private EditText editMessageTextfield;
    private ListView chatListView;

    //adapter
    private ChatMessageAdapter chatMessageAdapter;

    //misc
    private boolean isScrolling = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        chatView = inflater.inflate(R.layout.chat, container, false);
        return chatView;
    }

    @Override
    public void onActivityCreated(final Bundle savedState) {
        super.onActivityCreated(savedState);

        chatMessageAdapter = new ChatMessageAdapter(getActivity(), R.layout.chatmessage, chatModel.getChatMessages());

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
                serverPuller.addOutGoingMessageAndTriggerRequest(message);
                editMessageTextfield.setText("");
            }
        });
    }

    @SuppressWarnings("unused")
    public void onEvent(NewServerResponseEvent e) {
        refreshView();
    }

    private void refreshView() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                chatMessageAdapter = new ChatMessageAdapter(getActivity(), R.layout.chatmessage, chatModel.getChatMessages());
                chatListView.setAdapter(chatMessageAdapter);

                if (!isScrolling)
                    chatListView.setSelection(chatListView.getCount());
            }
        });
    }
}