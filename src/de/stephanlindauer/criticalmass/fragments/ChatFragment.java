package de.stephanlindauer.criticalmass.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import de.stephanlindauer.criticalmass.R;
import de.stephanlindauer.criticalmass.adapter.ChatMessageAdapter;
import de.stephanlindauer.criticalmass.model.ChatModel;
import de.stephanlindauer.criticalmass.vo.ChatMessage;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ChatFragment extends SuperFragment {

    private View chatView;
    private ChatMessageAdapter chatMessageAdapter;
    private ChatModel chatModel = ChatModel.getInstance();
    private ListView chatListView;
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


        Timer timerRefreshView = new Timer();
        TimerTask timerTaskRefreshView = new TimerTask() {
            @Override
            public void run() {
                try {
                    refreshView();
                } catch (Exception e) {
                }
            }
        };
        timerRefreshView.scheduleAtFixedRate(timerTaskRefreshView, 0, 20 * 1000);
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