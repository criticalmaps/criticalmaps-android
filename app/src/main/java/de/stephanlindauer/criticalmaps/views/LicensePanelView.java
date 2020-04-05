package de.stephanlindauer.criticalmaps.views;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.StringRes;

import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.databinding.ViewLicensePanelBinding;
import de.stephanlindauer.criticalmaps.utils.IntentUtil;

public class LicensePanelView extends LinearLayout {

    private ViewLicensePanelBinding binding;

    public LicensePanelView(Context context) {
        this(context, null);
    }

    public LicensePanelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LicensePanelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(VERTICAL);

        binding = ViewLicensePanelBinding.inflate(LayoutInflater.from(context), this);

        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.LicensePanelView, defStyleAttr, 0);
        int nameRes = a.getResourceId(R.styleable.LicensePanelView_name, 0);
        String linkString = a.getString(R.styleable.LicensePanelView_link);
        int copyrightRes = a.getResourceId(R.styleable.LicensePanelView_copyright, 0);
        int noticeRes = a.getResourceId(R.styleable.LicensePanelView_notice, 0);
        a.recycle();

        setViewTextFromResId(binding.licensepanelNameText, nameRes);
        setViewTextFromResId(binding.licensepanelCopyrightText, copyrightRes);
        setViewTextFromResId(binding.licensepanelNoticeText, noticeRes);

        if (linkString != null) {
            binding.licensepanelLinkText.setOnClickListener(
                    new IntentUtil.URLOpenOnActivityOnClickListener(linkString));
        }

        // make html links in notice text clickable
        binding.licensepanelNoticeText.setMovementMethod(LinkMovementMethod.getInstance());

        binding.licensepanelExpandcollapseText.setOnClickListener(v -> togglePanel());
    }

    private void setViewTextFromResId(TextView view, @StringRes int res) {
        if (res != 0) {
            view.setText(res);
        }
    }

    private void togglePanel() {
        if (binding.licensepanelNoticeText.getVisibility() == View.GONE) {
            binding.licensepanelNoticeText.setVisibility(VISIBLE);
            binding.licensepanelExpandcollapseText.setText(R.string.about_license_less);
        } else {
            binding.licensepanelNoticeText.setVisibility(GONE);
            binding.licensepanelExpandcollapseText.setText(R.string.about_license_more);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        binding = null;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        return new SavedState(superState, binding.licensepanelNoticeText.getVisibility());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        // hide the fade in animation by temporarily setting the duration to 0
        LayoutTransition layoutTransition = getLayoutTransition();
        if (layoutTransition != null) {
            long durationAppearing = layoutTransition.getDuration(LayoutTransition.APPEARING);
            layoutTransition.setDuration(LayoutTransition.APPEARING, 0);
            binding.licensepanelNoticeText.setVisibility(savedState.getVisibility());
            layoutTransition.setDuration(LayoutTransition.APPEARING, durationAppearing);
        } else {
            binding.licensepanelNoticeText.setVisibility(savedState.getVisibility());
        }
    }

    // we handle child states ourselves
    @Override
    protected void dispatchSaveInstanceState(SparseArray<android.os.Parcelable> container) {
        super.dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<android.os.Parcelable> container) {
        super.dispatchThawSelfOnly(container);
    }

    protected static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        private final int visibility;

        private SavedState(Parcel in) {
            super(in);
            visibility = in.readInt();
        }

        private SavedState(Parcelable superState, int visibility) {
            super(superState);
            this.visibility = visibility;
        }

        int getVisibility() {
            return visibility;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(visibility);
        }
    }
}
