package de.stephanlindauer.criticalmaps.views;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StringRes;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.utils.IntentUtil;


public class LicensePanelView extends LinearLayout {

    @Bind(R.id.about_license_name)
    TextView nameView;

    @Bind(R.id.about_license_link)
    TextView linkView;

    @Bind(R.id.about_license_copyright)
    TextView copyrightView;

    @Bind(R.id.about_license_notice)
    TextView noticeView;

    @Bind(R.id.about_license_expandcollapse)
    TextView expandCollapseView;


    public LicensePanelView(Context context) {
        this(context, null);
    }

    public LicensePanelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LicensePanelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.license_panel_view, this, true);
        ButterKnife.bind(this);

        LayoutTransition layoutTransition = getLayoutTransition();
        if (layoutTransition != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            // workaround to prevent clipping issues when there is no CHANGING
            // animation on the parent layout
            layoutTransition.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
            layoutTransition.setAnimator(LayoutTransition.DISAPPEARING, null);
            setLayoutTransition(layoutTransition);
        }

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LicensePanelView, defStyleAttr, 0);
        int nameRes = a.getResourceId(R.styleable.LicensePanelView_name, 0);
        String linkString = a.getString(R.styleable.LicensePanelView_link);
        int copyrightRes = a.getResourceId(R.styleable.LicensePanelView_copyright, 0);
        int noticeRes = a.getResourceId(R.styleable.LicensePanelView_notice, 0);
        a.recycle();

        setViewTextFromResId(nameView, nameRes);
        setViewTextFromResId(copyrightView, copyrightRes);
        setViewTextFromResId(noticeView, noticeRes);

        if (linkString != null) {
            linkView.setOnClickListener(new IntentUtil.URLOpenOnActivityOnClickListener(linkString));
        }

        // make html links in notice text clickable
        noticeView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setViewTextFromResId(TextView view, @StringRes int res) {
        if (res != 0) {
            view.setText(res);
        }
    }

    @OnClick(R.id.about_license_expandcollapse)
    public void togglePanel() {
        if (noticeView.getVisibility() == View.GONE) {
            noticeView.setVisibility(VISIBLE);
            expandCollapseView.setText(R.string.about_license_less);
        } else {
            noticeView.setVisibility(GONE);
            expandCollapseView.setText(R.string.about_license_more);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ButterKnife.unbind(this);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        return new SavedState(superState, noticeView.getVisibility());
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
            //noinspection ResourceType
            noticeView.setVisibility(savedState.getVisibility());
            layoutTransition.setDuration(LayoutTransition.APPEARING, durationAppearing);
        }
    }

    // we handle child states ourselves
    @Override
    protected void dispatchSaveInstanceState(SparseArray container) {
        super.dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray container) {
        super.dispatchThawSelfOnly(container);
    }

    protected static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

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

        public int getVisibility() {
            return visibility;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(visibility);
        }
    }
}
