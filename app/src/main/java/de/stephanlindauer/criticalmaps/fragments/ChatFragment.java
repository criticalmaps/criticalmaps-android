package de.stephanlindauer.criticalmaps.fragments;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.Unbinder;
import com.squareup.otto.Subscribe;

import org.ligi.axt.AXT;
import org.ligi.axt.simplifications.SimpleTextWatcher;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.adapter.ChatMessageAdapter;
import de.stephanlindauer.criticalmaps.events.NetworkConnectivityChangedEvent;
import de.stephanlindauer.criticalmaps.events.NewLocationEvent;
import de.stephanlindauer.criticalmaps.events.NewServerResponseEvent;
import de.stephanlindauer.criticalmaps.interfaces.IChatMessage;
import de.stephanlindauer.criticalmaps.model.ChatModel;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
import de.stephanlindauer.criticalmaps.provider.EventBusProvider;
import de.stephanlindauer.criticalmaps.vo.chat.OutgoingChatMessage;

public class ChatFragment extends Fragment {

    //dependencies
    @Inject
    ChatModel chatModel;

    @Inject
    EventBusProvider eventService;

    @Inject
    OwnLocationModel ownLocationModel;

    //view
    @BindView(R.id.chat_recycler)
    RecyclerView chatRecyclerView;

    @BindView(R.id.text_input_layout)
    TextInputLayout textInputLayout;

    @BindView(R.id.chat_edit_message)
    EditText editMessageTextField;

    @BindView(R.id.chat_send_btn)
    FloatingActionButton sendButton;

    //misc
    private boolean isScrolling = false;
    private boolean isTextInputEnabled = true;
    private boolean isDataConnectionAvailable = true;
    private Unbinder unbinder;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        App.components().inject(this);
        View chatView = inflater.inflate(R.layout.fragment_chat, container, false);
        unbinder = ButterKnife.bind(this, chatView);

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return chatView;
    }

    @Override
    public void onActivityCreated(final Bundle savedState) {
        super.onActivityCreated(savedState);

        chatModelToAdapter();

        textInputLayout.setCounterMaxLength(IChatMessage.MAX_LENGTH);
        editMessageTextField.setFilters(new InputFilter[]{new InputFilter.LengthFilter(IChatMessage.MAX_LENGTH)});

        Drawable wrappedDrawable = DrawableCompat.wrap(sendButton.getDrawable());
        DrawableCompat.setTintMode(wrappedDrawable, PorterDuff.Mode.SRC_ATOP);
        ColorStateList colorStateList = ContextCompat.getColorStateList(getActivity(),
                R.color.chat_fab_drawable_states);
        DrawableCompat.setTintList(wrappedDrawable, colorStateList);
        sendButton.setImageDrawable(wrappedDrawable);
        sendButton.setEnabled(false);

        chatRecyclerView.setOnTouchListener(new View.OnTouchListener() {
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

        editMessageTextField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    handleSendClicked();
                    return true;
                }
                return false;
            }
        });

        editMessageTextField.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                setSendButtonEnabledWithAnimation(s.length() != 0);
            }
        });
    }

    private void setSendButtonEnabledWithAnimation(final boolean enabled) {
        if (sendButton.isEnabled() == enabled) {
            return;
        }

        final AnimatorSet animatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(),
                R.animator.chat_fab_state_change);
        animatorSet.setTarget(sendButton);

        // flip button state for color change after first half of the animation
        final ArrayList<Animator> animations = animatorSet.getChildAnimations();
        animations.get(0).addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                sendButton.setEnabled(enabled);
            }
        });

        animatorSet.start();
    }

    @OnClick(R.id.chat_send_btn)
    void handleSendClicked() {
        String message = editMessageTextField.getText().toString();

        if (message.isEmpty()) {
            return;
        }

        chatModel.setNewOutgoingMessage(new OutgoingChatMessage(message));

        editMessageTextField.setText("");
        chatModelToAdapter();
    }

    private void chatModelToAdapter() {
        final ArrayList<IChatMessage> savedAndOutgoingMessages = chatModel.getSavedAndOutgoingMessages();
        chatRecyclerView.setAdapter(new ChatMessageAdapter(savedAndOutgoingMessages));

        if (!isScrolling) {
            chatRecyclerView.scrollToPosition(savedAndOutgoingMessages.size() - 1);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        chatModelToAdapter();
        eventService.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        eventService.unregister(this);
        AXT.at(editMessageTextField).hideKeyBoard();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Subscribe
    public void handleNewServerData(NewServerResponseEvent e) {
        chatModelToAdapter();
    }

    @Subscribe
    public void handleNewLocation(NewLocationEvent e) {
        setTextInputState(ownLocationModel.ownLocation != null, isDataConnectionAvailable);
    }

    @Subscribe
    public void handleNetworkConnectivityChanged(NetworkConnectivityChangedEvent e) {
        isDataConnectionAvailable = e.isConnected;
        setTextInputState(ownLocationModel.ownLocation != null, isDataConnectionAvailable);
    }

    private void setTextInputState(final boolean locationKnown, final boolean dataEnabled) {
        if (!locationKnown || !dataEnabled) {
            // FIXME fix this hacky mess of state handling
            setSendButtonEnabledWithAnimation(false);
            editMessageTextField.setEnabled(false);
            if (dataEnabled) {
                textInputLayout.setHint(getString(R.string.map_searching_for_location));
            } else {
                textInputLayout.setHint(getString(R.string.chat_no_data_connection_hint));
            }
            isTextInputEnabled = false;
        } else if (!isTextInputEnabled) {
            if (editMessageTextField.getText().length() > 0) {
                setSendButtonEnabledWithAnimation(true);
            }
            editMessageTextField.setEnabled(true);
            textInputLayout.setHint(getString(R.string.chat_text));
            isTextInputEnabled = true;
        }
    }
}
