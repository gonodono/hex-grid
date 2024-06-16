package com.gonodono.hexgrid.demo.internal

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.ViewSwitcher
import com.gonodono.hexgrid.demo.R

class DualPaneView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null
) : ViewSwitcher(context, attrs) {

    private val inLeftAnimation =
        loadAnimation(context, R.anim.slide_in_left)
    private val outLeftAnimation =
        loadAnimation(context, R.anim.slide_out_left)
    private val inRightAnimation =
        loadAnimation(context, R.anim.slide_in_right)
    private val outRightAnimation =
        loadAnimation(context, R.anim.slide_out_right)

    private var animating = false

    private val animationListener = object : AnimationListenerAdapter {
        override fun onAnimationEnd(anim: Animation) {
            animating = false
        }
    }

    override fun setDisplayedChild(whichChild: Int) {
        setChildAndAnimate(whichChild.coerceIn(0..1), true)
    }

    private fun setChildAndAnimate(whichChild: Int, doAnimate: Boolean) {
        if (displayedChild == whichChild) return
        if (doAnimate) {
            setAnimationForChild(whichChild)
        } else {
            setAnimationForChild(NONE)
        }
        animating = doAnimate
        super.setDisplayedChild(whichChild)
    }

    private fun setAnimationForChild(whichChild: Int) {
        when (whichChild) {
            CHILD_ONE -> {
                inAnimation = inLeftAnimation
                outAnimation = outRightAnimation
                inRightAnimation.setAnimationListener(null)
                inLeftAnimation.setAnimationListener(animationListener)
            }

            CHILD_TWO -> {
                inAnimation = inRightAnimation
                outAnimation = outLeftAnimation
                inRightAnimation.setAnimationListener(animationListener)
                inLeftAnimation.setAnimationListener(null)
            }

            else -> {
                inAnimation = null
                outAnimation = null
                inRightAnimation.setAnimationListener(null)
                inLeftAnimation.setAnimationListener(null)
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean =
        animating || super.onInterceptTouchEvent(ev)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        check(childCount == 2) {
            val name = DualPaneView::class.java.simpleName
            "$name must have exactly two children"
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.whichChild = displayedChild
        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        setChildAndAnimate(ss.whichChild, false)
    }

    private class SavedState : BaseSavedState {

        var whichChild: Int = 0

        constructor(superState: Parcelable?) : super(superState)

        private constructor(`in`: Parcel) : super(`in`) {
            whichChild = `in`.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(whichChild)
        }

        companion object {
            @Suppress("unused")
            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    companion object {
        private const val CHILD_ONE: Int = 0
        private const val CHILD_TWO: Int = 1
        private const val NONE = -1
    }
}

private interface AnimationListenerAdapter : AnimationListener {
    override fun onAnimationEnd(anim: Animation) {}
    override fun onAnimationStart(anim: Animation) {}
    override fun onAnimationRepeat(anim: Animation) {}
}