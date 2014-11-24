package de.stephanlindauer.criticalmaps.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import com.squareup.otto.Subscribe;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.adapter.ChatMessageAdapter;
import de.stephanlindauer.criticalmaps.events.NewLocationEvent;
import de.stephanlindauer.criticalmaps.events.NewServerResponseEvent;
import de.stephanlindauer.criticalmaps.model.ChatModel;
import de.stephanlindauer.criticalmaps.service.EventService;
import de.stephanlindauer.criticalmaps.service.ServerPuller;
import de.stephanlindauer.criticalmaps.vo.chat.OutgoingChatMessage;

public class ChatFragment extends SuperFragment {

    //dependencies
    private final ChatModel chatModel = ChatModel.getInstance();
    private final ServerPuller serverPuller = ServerPuller.getInstance();
    private final EventService eventService = EventService.getInstance();

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

        chatMessageAdapter = new ChatMessageAdapter(getActivity(), R.layout.chatmessage, chatModel.getSavedAndOutgoingMessages());

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

    private void refreshView() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                chatMessageAdapter = new ChatMessageAdapter(getActivity(), 123, chatModel.getSavedAndOutgoingMessages());
                chatListView.setAdapter(chatMessageAdapter);

                if (!isScrolling)
                    chatListView.setSelection(chatListView.getCount());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshView();
        eventService.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        eventService.unregister(this);
    }

    @Subscribe
    public void handleNewServerData(NewServerResponseEvent e) {
        refreshView();
    }

}