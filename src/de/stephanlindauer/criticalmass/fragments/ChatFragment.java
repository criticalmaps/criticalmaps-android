package de.stephanlindauer.criticalmass.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import de.stephanlindauer.criticalmass.R;
import de.stephanlindauer.criticalmass.adapter.ChatMessageAdapter;
import de.stephanlindauer.criticalmass.model.ChatModel;
import de.stephanlindauer.criticalmass.model.OwnLocationModel;
import de.stephanlindauer.criticalmass.service.GPSMananger;
import de.stephanlindauer.criticalmass.vo.ChatMessage;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ChatFragment extends SuperFragment {

    private View chatView;
    private ChatMessageAdapter chatMessageAdapter;
    private ChatModel chatModel = ChatModel.getInstance();
    private ListView chatList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        chatView = inflater.inflate(R.layout.chat, container, false);
        return chatView;
    }

    @Override
    public void onActivityCreated(final Bundle savedState) {
        super.onActivityCreated(savedState);

        chatMessageAdapter = new ChatMessageAdapter(getActivity(), R.layout.chatmessage, chatModel.getChatMessages());

        chatList = (ListView) getActivity().findViewById(R.id.chat_list);
        chatList.setAdapter( chatMessageAdapter );

        chatMessageAdapter.notifyDataSetChanged();

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
        timerRefreshView.scheduleAtFixedRate(timerTaskRefreshView, 2000, 10 * 1000);

    }

    private void refreshView() {
        chatMessageAdapter.notifyDataSetChanged();
    }
}