package net.lucode.hackware.magicindicator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;


/**
 * 使得MagicIndicator在FragmentContainer中使用
 * Created by hackware on 2016/9/4.
 */

public class FragmentContainerHelper extends AnimatorListenerAdapter implements ValueAnimator.AnimatorUpdateListener {
    private MagicIndicator mMagicIndicator;
    private ValueAnimator mScrollAnimator;
    private int mLastSelectedIndex;
    private int mDuration = 150;
    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    public FragmentContainerHelper(MagicIndicator magicIndicator) {
        mMagicIndicator = magicIndicator;
    }

    public void handlePageSelected(int selectedIndex) {
        handlePageSelected(selectedIndex, true);
    }

    public void handlePageSelected(int selectedIndex, boolean smooth) {
        if (mLastSelectedIndex == selectedIndex) {
            return;
        }
        if (smooth) {
            if (mScrollAnimator == null || !mScrollAnimator.isRunning()) {
                dispatchPageScrollStateChanged(ScrollState.SCROLL_STATE_SETTLING);
            }
            dispatchPageSelected(selectedIndex);
            float currentPositionOffsetSum = mLastSelectedIndex;
            if (mScrollAnimator != null) {
                currentPositionOffsetSum = (Float) mScrollAnimator.getAnimatedValue();
                mScrollAnimator.cancel();
                mScrollAnimator = null;
            }
            mScrollAnimator = new ValueAnimator();
            mScrollAnimator.setFloatValues(currentPositionOffsetSum, selectedIndex);    // position = selectedIndex, positionOffset = 0.0f
            mScrollAnimator.addUpdateListener(this);
            mScrollAnimator.addListener(this);
            mScrollAnimator.setInterpolator(mInterpolator);
            mScrollAnimator.setDuration(mDuration);
            mScrollAnimator.start();
        } else {
            dispatchPageSelected(selectedIndex);
            if (mScrollAnimator != null && mScrollAnimator.isRunning()) {
                dispatchPageScrolled(mLastSelectedIndex, 0.0f, 0);
            }
            dispatchPageScrollStateChanged(ScrollState.SCROLL_STATE_IDLE);
            dispatchPageScrolled(selectedIndex, 0.0f, 0);
        }
        mLastSelectedIndex = selectedIndex;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public void setInterpolator(Interpolator interpolator) {
        if (interpolator == null) {
            mInterpolator = new AccelerateDecelerateInterpolator();
        } else {
            mInterpolator = interpolator;
        }
    }

    private void dispatchPageSelected(int pageIndex) {
        mMagicIndicator.onPageSelected(pageIndex);
    }

    private void dispatchPageScrollStateChanged(int state) {
        mMagicIndicator.onPageScrollStateChanged(state);
    }

    private void dispatchPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mMagicIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels);
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float positionOffsetSum = (Float) animation.getAnimatedValue();
        int position = (int) positionOffsetSum;
        float positionOffset = positionOffsetSum - position;
        if (positionOffsetSum < 0) {
            position = -1;
            positionOffset = 1.0f + positionOffset;
        }
        dispatchPageScrolled(position, positionOffset, 0);
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        mMagicIndicator.onPageScrollStateChanged(ScrollState.SCROLL_STATE_IDLE);
        mScrollAnimator = null;
    }
}
