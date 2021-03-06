package com.ciandt.dragonfly.example.components.classifications

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.components.chips.Chip
import com.ciandt.dragonfly.example.components.chips.ChipsView
import com.ciandt.dragonfly.example.infrastructure.extensions.getLayoutInflaterService

class ClassificationsView : LinearLayout {

    private lateinit var titleView: TextView
    private lateinit var chipsView: ChipsView

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
        orientation = VERTICAL

        val inflater = context.getLayoutInflaterService()
        inflater.inflate(R.layout.component_classifications_view, this)

        titleView = findViewById(R.id.titleView)
        chipsView = findViewById(R.id.chipsView)
    }

    private fun initializeAttributes(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ClassificationsView, 0, 0)
        try {
            val title = typedArray.getString(R.styleable.ClassificationsView_title)
            setTitle(title)
        } finally {
            typedArray.recycle()
        }
    }

    fun setTitle(title: String) {
        titleView.text = title
    }

    fun setChips(chips: List<Chip>) {
        chipsView.setChips(chips)
    }

    fun addChip(index: Int, chip: Chip) {
        chipsView.addChip(index, chip)
    }

    fun removeChip(index: Int) {
        chipsView.removeChip(index)
    }

    fun removeChip(chip: Chip) {
        chipsView.removeChip(chip)
    }

    fun getChips(): List<Chip> {
        return chipsView.getChips()
    }

    fun select(chip: Chip) {
        chipsView.select(chip)
    }

    fun select(position: Int) {
        chipsView.select(position)
    }

    fun deselectAll() {
        chipsView.deselectAll()
    }

    fun setSelectable(selectable: Boolean) {
        chipsView.setSelectable(selectable)
    }

    fun setSelectCallback(callback: ((Chip) -> Unit)?) {
        chipsView.setSelectCallback(callback)
    }

    fun setDeselectCallback(callback: ((Chip) -> Unit)?) {
        chipsView.setDeselectCallback(callback)
    }
}