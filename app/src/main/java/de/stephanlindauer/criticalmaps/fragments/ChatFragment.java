package de.stephanlindauer.criticalmaps.fragments;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.otto.Subscribe;

import org.ligi.axt.AXT;
import org.ligi.axt.simplifications.SimpleTextWatcher;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.Unbinder;
import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.adapter.ChatMessageAdapter;
import de.stephanlindauer.criticalmaps.events.NetworkConnectivityChangedEvent;
import de.stephanlindauer.criticalmaps.events.NewServerResponseEvent;
import de.stephanlindauer.criticalmaps.interfaces.IChatMessage;
import de.stephanlindauer.criticalmaps.model.ChatModel;
import de.stephanlindauer.criticalmaps.model.chat.OutgoingChatMessage;
import de.stephanlindauer.criticalmaps.provider.EventBus;

public class ChatFragment extends Fragment {

    //dependencies
    @Inject
    ChatModel chatModel;

    @Inject
    EventBus eventBus;

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
    private boolean isTextInputEnabled = true;
    private ChatMessageAdapter chatMessageAdapter;
    private Unbinder unbinder;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
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

        chatMessageAdapter = new ChatMessageAdapter(new ArrayList<>());
        chatRecyclerView.setAdapter(chatMessageAdapter);
        displayNewData();

        textInputLayout.setCounterMaxLength(IChatMessage.MAX_LENGTH);
        editMessageTextField.setFilters(
                new InputFilter[]{new InputFilter.LengthFilter(IChatMessage.MAX_LENGTH)});
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        final String message = editMessageTextField.getText().toString();
        sendButton.setEnabled(!message.trim().isEmpty());

        editMessageTextField.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updateSendButtonEnabledState();
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

    @OnEditorAction(R.id.chat_edit_message)
    boolean handleEditorAction(int actionId) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            handleSendClicked();
            return true;
        }
        return false;
    }

    @OnClick(R.id.chat_send_btn)
    void handleSendClicked() {
        final String message = editMessageTextField.getText().toString().trim();

        if (message.isEmpty()) {
            return;
        }

        chatModel.setNewOutgoingMessage(new OutgoingChatMessage(message));

        editMessageTextField.setText("");
        displayNewData();
    }

    private void displayNewData() {
        final List<IChatMessage> savedAndOutgoingMessages = chatModel.getSavedAndOutgoingMessages();
        chatMessageAdapter.updateData(savedAndOutgoingMessages);

        if (chatRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
            chatRecyclerView.scrollToPosition(savedAndOutgoingMessages.size() - 1);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        displayNewData();
        eventBus.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        eventBus.unregister(this);
        AXT.at(editMessageTextField).hideKeyBoard();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void handleNewServerData(NewServerResponseEvent e) {
        displayNewData();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void handleNetworkConnectivityChanged(NetworkConnectivityChangedEvent e) {
        setTextInputState(e.isConnected);
    }

    private void setTextInputState(final boolean dataEnabled) {
        if (!dataEnabled) {
            setSendButtonEnabledWithAnimation(false);
            editMessageTextField.setEnabled(false);
            textInputLayout.setHint(getString(R.string.chat_no_data_connection_hint));
            isTextInputEnabled = false;
        } else if (!isTextInputEnabled) {
            updateSendButtonEnabledState();
            editMessageTextField.setEnabled(true);
            textInputLayout.setHint(getString(R.string.chat_text));
            isTextInputEnabled = true;
        }
    }

    private void updateSendButtonEnabledState() {
        final String message = editMessageTextField.getText().toString();
        setSendButtonEnabledWithAnimation(!message.trim().isEmpty());
    }
}
