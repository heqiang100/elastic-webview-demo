package com.example.elasticwebview;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.animation.ValueAnimator;
import android.view.animation.DecelerateInterpolator;

public class ElasticWebView extends WebView {
    private float startY;
    private float lastY;
    private float totalDelta;
    private boolean isScrolling;
    private float maxOverScrollDistance;
    private ValueAnimator bounceAnimator;
    
    public ElasticWebView(Context context) {
        super(context);
        init();
    }
    
    private void init() {
        // 禁用原生的回弹效果
        setOverScrollMode(OVER_SCROLL_NEVER);
        maxOverScrollDistance = dpToPx(100); // 最大拉伸距离
        
        // 处理触摸事件
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = event.getY();
                        lastY = startY;
                        isScrolling = false;
                        cancelBounceAnimation();
                        break;
                        
                    case MotionEvent.ACTION_MOVE:
                        float currentY = event.getY();
                        float deltaY = currentY - lastY;
                        lastY = currentY;
                        
                        // 检查是否在边界
                        if (isAtTop() && deltaY > 0 || isAtBottom() && deltaY < 0) {
                            if (!isScrolling) {
                                isScrolling = true;
                                totalDelta = 0;
                            }
                            totalDelta += deltaY;
                            applyBounceEffect();
                            return true;
                        }
                        break;
                        
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (isScrolling) {
                            startBounceAnimation();
                            return true;
                        }
                        break;
                }
                return false;
            }
        });
    }
    
    private boolean isAtTop() {
        return getScrollY() == 0;
    }
    
    private boolean isAtBottom() {
        return getScrollY() + getHeight() >= getContentHeight() * getScale();
    }
    
    private void applyBounceEffect() {
        float scale = 1 + Math.min(
            Math.abs(totalDelta) / maxOverScrollDistance, 
            0.15f
        );
        setScaleY(scale);
        setPivotY(totalDelta > 0 ? 0 : getHeight());
    }
    
    private void startBounceAnimation() {
        if (bounceAnimator != null) {
            bounceAnimator.cancel();
        }
        
        bounceAnimator = ValueAnimator.ofFloat(getScaleY(), 1f);
        bounceAnimator.setDuration(300);
        bounceAnimator.setInterpolator(new DecelerateInterpolator());
        bounceAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setScaleY((float) animation.getAnimatedValue());
            }
        });
        bounceAnimator.start();
    }
    
    private void cancelBounceAnimation() {
        if (bounceAnimator != null) {
            bounceAnimator.cancel();
        }
    }
    
    private float dpToPx(float dp) {
        return dp * getContext().getResources().getDisplayMetrics().density;
    }
}