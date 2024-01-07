package com.ljx.layoutmanager;

import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import androidx.recyclerview.widget.SnapHelper;


/**
 * User: ljx
 * Date: 2023/11/20
 * Time: 21:25
 */
public class PageSnapHelper extends SnapHelper {

    private OrientationHelper mHorizontalHelper, mVerticalHelper;
    private RecyclerView mRecyclerView;

    @Override
    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) throws IllegalStateException {
        this.mRecyclerView = recyclerView;
        super.attachToRecyclerView(recyclerView);
    }

    @Override
    public int findTargetSnapPosition(LayoutManager layoutManager, int velocityX, int velocityY) {
        if (layoutManager instanceof GridPagerLayoutManager) {
            int targetPosition = findSnapPosition(layoutManager);
            if (targetPosition != -1) {
                if (layoutManager.canScrollHorizontally()) {
                    return velocityX > 0 ? targetPosition : targetPosition - 1;
                } else {
                    return velocityY > 0 ? targetPosition : targetPosition - 1;
                }
            }
        }
        return -1;
    }

    private int findSnapPosition(LayoutManager layoutManager) {
        int childCount = layoutManager.getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            View view = layoutManager.getChildAt(i);
            if (view == null) continue;
            GridPagerLayoutManager.LayoutParams layoutParams = (GridPagerLayoutManager.LayoutParams) view.getLayoutParams();
            if (layoutParams.isPageHead()) {
                return layoutManager.getPosition(view);
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull LayoutManager layoutManager, @NonNull View targetView) {
        int[] out = new int[2];
        if (layoutManager instanceof GridPagerLayoutManager) {
            if (layoutManager.canScrollHorizontally()) {
                out[0] = calculateDistance(targetView, layoutManager, getHorizontalHelper(layoutManager));
            }
            if (layoutManager.canScrollVertically()) {
                out[1] = calculateDistance(targetView, layoutManager, getVerticalHelper(layoutManager));
            }
        }
        return out;
    }

    private int calculateDistance(View targetView, LayoutManager layoutManager, OrientationHelper helper) {
        if (helper == null) return 0;
        GridPagerLayoutManager.LayoutParams layoutParams = (GridPagerLayoutManager.LayoutParams) targetView.getLayoutParams();
        int position = layoutManager.getPosition(targetView);
        if (layoutParams.isPageHead()) {
            //目标view到左边或顶部的距离
            return helper.getDecoratedStart(targetView) - helper.getStartAfterPadding();
        } else { //TODO
            //目标view到右边或底部的距离，注意这里要取反
            return -helper.getEndAfterPadding() + helper.getDecoratedEnd(targetView);
        }
    }

    @Override
    public View findSnapView(LayoutManager layoutManager) {
        if (layoutManager instanceof GridPagerLayoutManager) {
            OrientationHelper orientationHelper = getOrientationHelper(layoutManager);
            if (orientationHelper == null) return null;
            int targetPosition = findSnapPosition(layoutManager);
            if (targetPosition == -1) return null;
            View view = layoutManager.findViewByPosition(targetPosition);
            if (view == null) return null;
            //移动的距离
            int offsetX = orientationHelper.getDecoratedStart(view) - orientationHelper.getStartAfterPadding();
            int totalSpace = layoutManager.getClipToPadding() ? orientationHelper.getTotalSpace() : orientationHelper.getEnd();
            if (offsetX > totalSpace / 2) {
                return layoutManager.findViewByPosition(targetPosition - 1);
            } else {
                return view;
            }
        }
        return null;
    }

    protected RecyclerView.SmoothScroller createScroller(
        @NonNull LayoutManager layoutManager) {
        if (!(layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider)) {
            return null;
        }
        return new LinearSmoothScroller(mRecyclerView.getContext()) {

            static final float MILLISECONDS_PER_INCH = 100f;
            private static final int MAX_SCROLL_ON_FLING_DURATION = 100; // ms

            @Override
            protected void onTargetFound(@NonNull View targetView,
                                         @NonNull RecyclerView.State state, @NonNull Action action) {
                int[] snapDistances = calculateDistanceToFinalSnap(layoutManager, targetView);
                final int dx = snapDistances[0];
                final int dy = snapDistances[1];
                final int time = calculateTimeForDeceleration(Math.max(Math.abs(dx), Math.abs(dy)));
                if (time > 0) {
                    action.update(dx, dy, time, mDecelerateInterpolator);
                }
            }

            @Override
            protected float calculateSpeedPerPixel(@NonNull DisplayMetrics displayMetrics) {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
            }

            @Override
            protected int calculateTimeForScrolling(int dx) {
                return Math.min(MAX_SCROLL_ON_FLING_DURATION, super.calculateTimeForScrolling(dx));
            }
        };
    }

    @Nullable
    private OrientationHelper getOrientationHelper(LayoutManager layoutManager) {
        if (layoutManager.canScrollVertically()) {
            return getVerticalHelper(layoutManager);
        } else if (layoutManager.canScrollHorizontally()) {
            return getHorizontalHelper(layoutManager);
        } else {
            return null;
        }
    }

    private OrientationHelper getHorizontalHelper(
        @NonNull LayoutManager layoutManager) {
        if (mHorizontalHelper == null) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
        }
        return mHorizontalHelper;
    }

    private OrientationHelper getVerticalHelper(LayoutManager layoutManager) {
        if (mVerticalHelper == null) {
            mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager);
        }
        return mVerticalHelper;

    }
}



