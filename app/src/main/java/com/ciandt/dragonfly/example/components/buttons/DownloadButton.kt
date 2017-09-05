package com.ciandt.dragonfly.example.components.buttons

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.infrastructure.extensions.getLayoutInflaterService
import com.transitionseverywhere.ChangeBounds
import com.transitionseverywhere.Crossfade
import com.transitionseverywhere.Fade
import com.transitionseverywhere.Scene
import com.transitionseverywhere.Slide
import com.transitionseverywhere.Transition
import com.transitionseverywhere.TransitionManager
import com.transitionseverywhere.TransitionSet
import com.transitionseverywhere.extra.Scale

class DownloadButton : FrameLayout {

    sealed class State {
        abstract fun getViewIndex(): Int?

        data class Start(val icon: Drawable?, val text: String) : State() {
            override fun getViewIndex(): Int? = 0
        }

        class Progress : State() {
            override fun getViewIndex(): Int? = 2
        }

        class Done(val text: String) : State() {
            override fun getViewIndex(): Int? = 4
        }
    }

    private var state = 0

    private val views = mutableMapOf<Int, View>()

    constructor(context: Context) : super(context) {
        initializeViews(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initializeViews(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initializeViews(context)
    }

    private fun initializeViews(context: Context) {
        val inflater = context.getLayoutInflaterService()

        // Download w/ icon
        views[0] = inflater.inflate(R.layout.component_download_button_scene_0, this, false)

        // Icon
        views[1] = inflater.inflate(R.layout.component_download_button_scene_1, this, false)

        // Progress bar
        views[2] = inflater.inflate(R.layout.component_download_button_scene_2, this, false)

        // Check
        views[3] = inflater.inflate(R.layout.component_download_button_scene_3, this, false)

        // Explore
        views[4] = inflater.inflate(R.layout.component_download_button_scene_4, this, false)
    }

    private fun goTo(view: View?, transition: Transition? = null) {
        view ?: return

        val t = transition ?: TransitionSet().apply { duration = 0 }

        TransitionManager.go(Scene(this, view), t)
    }

    fun setState(state: State, animated: Boolean = false) {
        val from = this.state

        val index = state.getViewIndex() ?: -1
        if (index < 0 || index > views.size) {
            return
        }

        val view = views[index] ?: return

        when (state) {
            is State.Start -> {
                view.findViewById<ImageView>(R.id.iconStart)?.setImageDrawable(state.icon)
                view.findViewById<TextView>(R.id.labelStart)?.text = state.text
            }

            is State.Done -> {
                view.findViewById<TextView>(R.id.labelDone)?.text = state.text
            }
        }

        this.state = index

        if (!animated) {
            goTo(view)

        } else {
            when (from) {

                0 -> when (index) {
                    2 -> animateFrom0To2()
                }

                2 -> when (index) {
                    0 -> animateFrom2To0()
                    4 -> animateFrom2To4()
                }
            }
        }
    }

    fun changeState(state: State) {
        setState(state, true)
    }


    /**
     * From Start to Progress
     */
    private fun animateFrom0To2() {

        val set = TransitionSet().apply {

            addTransition(Slide(Gravity.LEFT).apply {
                addTarget(R.id.iconStart)
            })

            addTransition(ChangeBounds())

            ordering = TransitionSet.ORDERING_TOGETHER
            duration = 350

            onTransitionEnd({
                helper1To2()
            }, 100)
        }

        goTo(views[1], set)
    }

    private fun helper1To2() {

        val set = TransitionSet().apply {

            addTransition(Fade(Fade.IN).apply {
                addTarget(R.id.progress)
                addTarget(R.id.background)
            })

            addTransition(Scale(0.7f).apply {
                addTarget(R.id.iconStart)
                addTarget(R.id.progress)
            })

            addTransition(Fade(Fade.OUT).apply {
                addTarget(R.id.iconStart)
            })

            duration = 400
        }

        goTo(views[2], set)
    }


    /**
     * From Progress to Start
     */
    private fun animateFrom2To0() {

        val set = TransitionSet().apply {

            addTransition(Fade(Fade.OUT).apply {
                addTarget(R.id.progress)
                addTarget(R.id.background)
            })

            addTransition(Fade(Fade.IN).apply {
                addTarget(R.id.iconStart)
                addTarget(R.id.labelStart)
            })

            addTransition(Scale(0.7f).apply {
                addTarget(R.id.iconStart)
                addTarget(R.id.progress)
            })

            duration = 400
        }

        goTo(views[0], set)
    }


    /**
     * From Progress to Done
     */
    private fun animateFrom2To4() {

        val set = TransitionSet().apply {

            addTransition(Scale(0.7f).apply {
                addTarget(R.id.progress)
                addTarget(R.id.iconDone)
            })

            addTransition(Fade(Fade.IN).apply {
                addTarget(R.id.iconDone)
            })

            addTransition(Fade(Fade.OUT).apply {
                addTarget(R.id.progress)
            })

            addTransition(Crossfade().apply {
                addTarget(R.id.background)
            })

            duration = 600

            onTransitionEnd({
                helper3To4()
            }, 300)
        }

        goTo(views[3], set)
    }

    private fun helper3To4() {

        val set = TransitionSet().apply {

            addTransition(Fade(Fade.OUT).apply {
                addTarget(R.id.iconDone)
            })

            addTransition(Crossfade().apply {
                addTarget(R.id.background)
            })

            addTransition(Fade(Fade.IN).apply {
                addTarget(R.id.labelDone)
            })

            addTransition(Scale(0.7f).apply {
                addTarget(R.id.labelDone)
            })

            duration = 600
        }

        goTo(views[4], set)
    }

}

// Helper Extension
fun TransitionSet.onTransitionEnd(action: () -> Unit, delay: Long) {
    addListener(object : Transition.TransitionListenerAdapter() {
        override fun onTransitionEnd(transition: Transition?) {
            super.onTransitionEnd(transition)
            removeListener(this)
            Handler().postDelayed({ action() }, delay)
        }
    })
}