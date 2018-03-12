package com.mealant.steppers;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by sungtae.lim on 2018. 3. 8..
 */

public class HorizontalStepper extends ConstraintLayout implements ViewPager.OnPageChangeListener {

    private static final int ORDER_VIEW_ID = 1000;
    private static final int TITLE_TEXT_VIEW_ID = 2000;
    private static final int PROGRESS_VIEW_ID = 3000;

    private View[] orderViews;
    private ProgressBar[] progressBars;
    private TextView[] titleTextViews;
    private @Nullable
    ViewPager pager;

    public interface OnStepClickListener {
        void onStepClick(int position);
    }

    private OnStepClickListener stepClickListener;

    public HorizontalStepper(Context context) {
        super(context);
    }

    public HorizontalStepper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalStepper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPager(final @Nullable ViewPager pager) {
        this.pager = pager;
        setOnPageChangeListener();
        drawStepper();
    }

    private void setOnPageChangeListener() {
        if (pager != null) {
            pager.addOnPageChangeListener(this);
        }
    }

    public void setOnStepClickListener(final @Nullable OnStepClickListener stepClickListener) {
        this.stepClickListener = stepClickListener;
    }

    public @Nullable
    OnStepClickListener getOnStepClickListener() {
        return stepClickListener;
    }

    private void drawStepper() {
        removeAllViews();

        if (pager == null || pager.getAdapter() == null) {
            return;
        }

        final PagerAdapter adapter = pager.getAdapter();
        final int count = adapter.getCount();

        orderViews = new View[count];
        progressBars = new ProgressBar[count - 1];
        titleTextViews = new TextView[count];

        if (count == 0) {
            return;
        }

        final CharSequence[] pageTitles = new CharSequence[count];

        for (int i = 0; i < count; i++) {
            final int position = i;

            final View orderView = LayoutInflater.from(getContext()).inflate(R.layout.content_stepper_order, null);
            orderView.setId(ORDER_VIEW_ID + i);

            orderViews[position] = orderView;

            final TextView orderText = orderView.findViewById(R.id.stepper_order_text);

            orderText.setText(String.valueOf(position + 1));

            if (position > 0) {
                final View progressView = LayoutInflater.from(getContext()).inflate(R.layout.content_stepper_progress, null);
                progressView.setId(PROGRESS_VIEW_ID + i - 1);

                progressBars[i - 1] = progressView.findViewById(R.id.stepper_progress);

                final LayoutParams progressBarLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                progressBarLayoutParams.startToEnd = orderViews[position - 1].getId();
                progressView.setLayoutParams(progressBarLayoutParams);

                addView(progressView);

                final LayoutParams orderTextViewLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                orderTextViewLayoutParams.startToEnd = progressView.getId();

                orderViews[position].setLayoutParams(orderTextViewLayoutParams);
            }

            orderViews[position].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    pager.setCurrentItem(position, true);

                    if (stepClickListener != null) {
                        stepClickListener.onStepClick(position);
                    }
                }
            });

            addView(orderViews[position]);

            pageTitles[position] = adapter.getPageTitle(position);

            if (pageTitles[position] != null) {
                final int titleTextViewId = TITLE_TEXT_VIEW_ID + position;

                titleTextViews[position] = new TextView(getContext());
                titleTextViews[position].setId(titleTextViewId);
                titleTextViews[position].setText(pageTitles[position]);

                final LayoutParams titleTextViewLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                titleTextViewLayoutParams.topToBottom = orderView.getId();

                titleTextViews[position].setLayoutParams(titleTextViewLayoutParams);

                addView(titleTextViews[position]);

                if (position > 0) {
                    titleTextViewLayoutParams.startToEnd = titleTextViews[position - 1].getId();
                    titleTextViews[position].setLayoutParams(titleTextViewLayoutParams);
                }
            }
        }
    }

    private int prevPosition;

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (progressBars.length > 0) {
            if (positionOffset == 0f) {
//                progressBars[position].setProgress((int) (positionOffset * 100));
            } else if (prevPosition < position) {
                // swipe to left
                progressBars[prevPosition].setProgress((int) (positionOffset * 100));
            } else {
                // swipe to right
                progressBars[position].setProgress((int) (positionOffset * 100));
            }
        }
    }

    @Override
    public void onPageSelected(int position) {
        prevPosition = position;

        if (!isValidate()) {
            return;
        }
        @SuppressWarnings("ConstantConditions") final int count = pager.getAdapter().getCount();

        for (int i = 0; i < count; i++) {
            orderViews[i].setSelected(i == position);
        }
    }


    @Override
    public void onPageScrollStateChanged(int state) {
    }

    private boolean isValidate() {
        if (pager == null) {
            return false;
        }
        final PagerAdapter adapter = pager.getAdapter();

        if (adapter == null) {
            return false;
        }

        final int count = adapter.getCount();

        return orderViews.length == count && progressBars.length == count - 1 && titleTextViews.length == count;
    }

    public void setStep(final @IntRange(from = 0) int position) {
        prevPosition = position;

        if (position < 0 || !isValidate()) {
            return;
        }

        if (position >= pager.getAdapter().getCount()) {
            throw new IndexOutOfBoundsException();
        }

        @SuppressWarnings("ConstantConditions") final int count = pager.getAdapter().getCount();

        for (int i = 0; i < count; i++) {
            orderViews[i].setSelected(i == position);
        }

        pager.setCurrentItem(position);
    }
}
