package com.acs.btdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

public class CollapsingImageBehaviour extends CoordinatorLayout.Behavior<View> {
    private final static int X = 0;
    private final static int Y = 1;
    private final static int WIDTH = 2;
    private final static int HEIGHT = 3;

    private int mTargetId;

    private int[] mView;

    private int[] mTarget;

    public CollapsingImageBehaviour(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CollapsingImageBehavior);
            mTargetId = a.getResourceId(R.styleable.CollapsingImageBehavior_collapsedTarget, 0);
            a.recycle();
        }
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {

        setup(parent, child);

        AppBarLayout appBarLayout = (AppBarLayout) dependency;

        int range = appBarLayout.getTotalScrollRange();
        float factor = -appBarLayout.getY() / range;

        int left = mView[X] + (int) (factor * (mTarget[X] - mView[X]));
        int top = mView[Y] + (int) (factor * (mTarget[Y] - mView[Y]));
        int width = mView[WIDTH] + (int) (factor * (mTarget[WIDTH] - mView[WIDTH]));
        int height = mView[HEIGHT] + (int) (factor * (mTarget[HEIGHT] - mView[HEIGHT]));

        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        lp.width = width;
        lp.height = height;
        child.setLayoutParams(lp);
        child.setX(left);
        child.setY(top);

        return true;
    }

    private void setup(CoordinatorLayout parent, View child) {

        if (mView != null) return;

        mView = new int[4];
        mTarget = new int[4];

        mView[X] = (int) child.getX();
        mView[Y] = (int) child.getY();
        mView[WIDTH] = child.getWidth();
        mView[HEIGHT] = child.getHeight();

        View target = parent.findViewById(mTargetId);
        if (target == null) {
            throw new IllegalStateException("target view not found");
        }

        mTarget[WIDTH] += target.getWidth();
        mTarget[HEIGHT] += target.getHeight();

        View view = target;
        while (view != parent) {
            mTarget[X] += (int) view.getX();
            mTarget[Y] += (int) view.getY();
            view = (View) view.getParent();
        }

    }

}
