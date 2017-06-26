package com.ciandt.dragonfly.example.components.input

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.annotation.StyleRes
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.RelativeLayout
import android.widget.TextView.OnEditorActionListener
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.infrastructure.extensions.getLayoutInflaterService
import com.ciandt.dragonfly.example.infrastructure.extensions.toPx
import kotlinx.android.synthetic.main.component_autocomplete_textview.*
import kotlinx.android.synthetic.main.component_autocomplete_textview.view.*


class DragonflyAutoComplete : RelativeLayout {

    private var lineColor = Color.TRANSPARENT
    private var lineColorFocused = Color.TRANSPARENT

    private val lineHeight = 1.toPx()
    private val lineHeightFocused = 2.toPx()

    constructor(context: Context) : super(context) {
        initializeViews(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initializeViews(context)
        initializeAttributes(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initializeViews(context)
        initializeAttributes(attrs)
    }

    private fun initializeViews(context: Context) {
        val inflater = context.getLayoutInflaterService()
        inflater.inflate(R.layout.component_autocomplete_textview, this)
    }

    private fun initializeAttributes(attrs: AttributeSet) {

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DragonflyAutoComplete, 0, 0)
        try {

            val hint = typedArray.getString(R.styleable.DragonflyAutoComplete_hint)
            setHint(hint)

            val hintTextAppearance = typedArray.getResourceId(R.styleable.DragonflyAutoComplete_hintTextAppearance, -1)
            if (hintTextAppearance != -1) {
                setHintTextAppearance(hintTextAppearance)
            }

            val text = typedArray.getString(R.styleable.DragonflyAutoComplete_text)
            setText(text ?: "")

            val textAppearance = typedArray.getResourceId(R.styleable.DragonflyAutoComplete_textAppearance, -1)
            if (textAppearance != -1) {
                setTextAppearance(textAppearance)
            }

            val textMarginTop = typedArray.getDimensionPixelSize(R.styleable.DragonflyAutoComplete_textMarginTop, 0)
            val textMarginBottom = typedArray.getDimensionPixelSize(R.styleable.DragonflyAutoComplete_textMarginBottom, 0)
            setTextMargins(textMarginTop, textMarginBottom)

            lineColor = typedArray.getColor(R.styleable.DragonflyAutoComplete_lineColor, Color.TRANSPARENT)
            lineColorFocused = typedArray.getColor(R.styleable.DragonflyAutoComplete_lineColorFocused, Color.TRANSPARENT)

        } finally {
            typedArray.recycle()
        }
    }

    private fun getYToCenterHint(): Float {
        return inputText.y + (inputText.height / 2) - (inputHint.height / 2)
    }

    private fun onResume() {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                configView()
            }
        })

        inputText.setOnFocusChangeListener { _, hasFocus ->
            animate(hasFocus)
        }
    }

    private fun onPause() {
        inputText.onFocusChangeListener = null
    }

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)

        if (visibility == View.VISIBLE) {
            onResume()
        } else {
            onPause()
        }
    }

    private fun configView() {
        if (getText().isEmpty()) {
            inputLine.setBackgroundColor(lineColor)
            inputHint.translationY = getYToCenterHint()
        } else {
            inputLine.setBackgroundColor(lineColorFocused)
            inputHint.translationY = 0.0f
        }
    }

    private fun animate(hasFocus: Boolean, duration: Long = 250L) {

        if (inputText.text.isNotEmpty()) {
            return
        }

        val translationY = getYToCenterHint()
        val translationYFocused = 0.0f

        val animatorSet = AnimatorSet()

        if (hasFocus) {
            val translation = ObjectAnimator.ofFloat(inputHint, "translationY", translationY, translationYFocused)

            val height = ValueAnimator.ofInt(lineHeight, lineHeightFocused)
            height.addUpdateListener { animation ->
                inputLine.layoutParams.height = animation.animatedValue as Int
                inputLine.requestLayout()
            }

            val color = ObjectAnimator.ofObject(inputLine, "backgroundColor", ArgbEvaluator(), lineColor, lineColorFocused)

            animatorSet.playTogether(translation, color, height)

        } else {
            val translation = ObjectAnimator.ofFloat(inputHint, "translationY", translationYFocused, translationY)

            val height = ValueAnimator.ofInt(lineHeightFocused, lineHeight)
            height.addUpdateListener { animation ->
                inputLine.layoutParams.height = animation.animatedValue as Int
                inputLine.requestLayout()
            }

            val color = ObjectAnimator.ofObject(inputLine, "backgroundColor", ArgbEvaluator(), lineColorFocused, lineColor)

            animatorSet.playTogether(translation, color, height)
        }

        animatorSet.duration = duration
        animatorSet.start()
    }

    private fun setTextMargins(top: Int, bottom: Int) {
        val layoutParams = inputText.layoutParams as MarginLayoutParams
        layoutParams.setMargins(0, top, 0, bottom)
        inputText.layoutParams = layoutParams
    }

    fun setHint(text: String) {
        inputHint.text = text
    }

    fun setHintTextAppearance(style: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            inputHint.setTextAppearance(style)
        } else {
            inputHint.setTextAppearance(context, style)
        }
    }

    fun setText(text: String) {
        inputText.setText(text)
    }

    fun getText(): String {
        return inputText.text.toString()
    }

    fun setTextAppearance(@StyleRes resId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            inputText.setTextAppearance(resId)
        } else {
            inputText.setTextAppearance(context, resId)
        }
    }

    fun setOnEditorActionListener(listener: OnEditorActionListener) {
        inputText.setOnEditorActionListener(listener)
    }

    fun setOnTextChangedListener(watcher: TextWatcher) {
        inputText.addTextChangedListener(watcher)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        inputText.isEnabled = enabled
        inputText.isFocusable = enabled
        inputText.isFocusableInTouchMode = enabled
    }
}
