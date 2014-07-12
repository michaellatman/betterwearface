package com.michaellatman.betterwearface;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

/**
 * Created by michael on 7/8/14.
 */
public class AnimatedTextView extends TextView implements Animation.AnimationListener {
    private  Animation out;
    private  Animation in;
    String pending = "";
    Boolean isDirty = false;
    Boolean animated = true;
    public AnimatedTextView(Context context) {
        super(context);
        in = AnimationUtils.loadAnimation(context, R.anim.slidein);
        out = AnimationUtils.loadAnimation(context, R.anim.slideout);
        out.setFillAfter(true);
        in.setAnimationListener(this);
        out.setAnimationListener(this);
    }

    public AnimatedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        in = AnimationUtils.loadAnimation(context, R.anim.slidein);
        out = AnimationUtils.loadAnimation(context, R.anim.slideout);
        out.setFillAfter(true);
        in.setAnimationListener(this);
        out.setAnimationListener(this);
    }

    public AnimatedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        in = AnimationUtils.loadAnimation(context, R.anim.slidein);
        out = AnimationUtils.loadAnimation(context, R.anim.slideout);
        out.setFillAfter(true);
        in.setAnimationListener(this);
        out.setAnimationListener(this);
    }
    public void setIn(Animation a){
        in = a;
        in.setAnimationListener(this);
        out.setAnimationListener(this);
    }
    public void setOut(Animation a){
        out = a;
        in.setAnimationListener(this);
        out.setAnimationListener(this);
    }

    public void setPendingText(String pending){
        if(!getText().equals(pending)){
            if(animated){
                isDirty = true;
                this.pending = pending;
                animateOut();
            }
            else{
                setText(pending);
            }
        }
    }
    public void setAnimated(Boolean now){
        animated = now;
    }
    public void animateOut(){
        this.startAnimation(out);
    }
    public void animateIn(){
        this.startAnimation(in);
    }

    @Override
    public void onAnimationStart(Animation animation) {
        if(animation == in){
            //setVisibility(View.VISIBLE);
            Log.d("Animated", "In");
        }
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if(animation == out){
            //setVisibility(View.INVISIBLE);
            isDirty = false;
            setText(pending);
            this.startAnimation(in);
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
