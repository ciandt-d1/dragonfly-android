package com.ciandt.dragonfly.example.components.chips

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.infrastructure.extensions.getLayoutInflaterService
import com.ciandt.dragonfly.example.infrastructure.extensions.toPx
import kotlinx.android.synthetic.main.component_chips_view.view.*

class ChipsView : RelativeLayout {

    private val chips = ArrayList<Chip>()

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChipAdapter

    private var selectCallback: ((Chip) -> Unit)? = null
    private var deselectCallback: ((Chip) -> Unit)? = null

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
        inflater.inflate(R.layout.component_chips_view, this)

        adapter = ChipAdapter(context, chips) { chip, activated ->
            if (activated) {
                selectCallback?.invoke(chip)
            } else {
                deselectCallback?.invoke(chip)
            }
        }
        adapter.setHasStableIds(true)

        recyclerView = findViewById(R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        (recyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        recyclerView.adapter = adapter
        recyclerView.hasFixedSize()
    }

    private fun initializeAttributes(attrs: AttributeSet) {

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ChipsView, 0, 0)
        try {

            val chipLayout = typedArray.getResourceId(R.styleable.ChipsView_chipLayout, INVALID_RESOURCE)
            if (chipLayout != INVALID_RESOURCE) {
                setChipLayout(chipLayout)
            }

            val chipSpaceBefore = typedArray.getDimensionPixelSize(R.styleable.ChipsView_chipSpaceBefore, DEFAULT_SPACE_BEFORE)
            val chipSpaceBetween = typedArray.getDimensionPixelSize(R.styleable.ChipsView_chipSpaceBetween, DEFAULT_SPACE_BETWEEN)
            val chipSpaceAfter = typedArray.getDimensionPixelSize(R.styleable.ChipsView_chipSpaceAfter, DEFAULT_SPACE_AFTER)
            setChipSpace(chipSpaceBefore, chipSpaceBetween, chipSpaceAfter)

            val fadingEdges = typedArray.getBoolean(R.styleable.ChipsView_fadingEdges, false)
            setFadingEdges(fadingEdges)

            val fadingEdgesDrawable = typedArray.getDrawable(R.styleable.ChipsView_fadingEdgesDrawable)
            setFadingEdgesDrawable(fadingEdgesDrawable)

            val fadingEdgesWidth = typedArray.getDimensionPixelSize(R.styleable.ChipsView_fadingEdgesWidth, DEFAULT_EDGES_WIDTH)
            setFadingEdgesWidth(fadingEdgesWidth)

            val selectable = typedArray.getBoolean(R.styleable.ChipsView_selectable, false)
            setSelectable(selectable)

            val multipleSelection = typedArray.getBoolean(R.styleable.ChipsView_multipleSelection, false)
            setMultipleSelection(multipleSelection)

        } finally {
            typedArray.recycle()
        }
    }

    fun setChipLayout(layout: Int) {
        adapter.setChipLayout(layout)
    }

    fun setChipSpace(spaceBefore: Int, spaceBetween: Int, spaceAfter: Int) {
        recyclerView.addItemDecoration(SpaceItemDecoration(spaceBefore, spaceBetween, spaceAfter))
    }

    fun setFadingEdges(fadingEdges: Boolean) {
        fadingStart.visibility = if (fadingEdges) View.VISIBLE else View.INVISIBLE
        fadingEnd.visibility = if (fadingEdges) View.VISIBLE else View.INVISIBLE
    }

    fun setFadingEdgesDrawable(fadingEdgesDrawable: Drawable?) {
        fadingEdgesDrawable?.let {
            fadingStart.setImageDrawable(it)
            fadingEnd.setImageDrawable(it)
        }
    }

    fun setFadingEdgesWidth(fadingEdgesWidth: Int) {
        fadingStart.layoutParams.width = fadingEdgesWidth
        fadingEnd.layoutParams.width = fadingEdgesWidth
    }

    fun setSelectable(selectable: Boolean) {
        adapter.setSelectable(selectable)
        adapter.notifyDataSetChanged()
    }

    fun setMultipleSelection(multipleSelection: Boolean) {
        adapter.setMultipleSelection(multipleSelection)
    }

    fun setChips(chips: List<Chip>) {
        this.chips.clear()
        this.chips.addAll(chips)
        adapter.notifyDataSetChanged()
    }

    fun addChip(index: Int, chip: Chip) {
        chips.add(index, chip)
        adapter.notifyItemInserted(index)
    }

    fun removeChip(index: Int) {
        chips.removeAt(index)
        adapter.notifyItemRemoved(index)
    }

    fun removeChip(chip: Chip) {
        val index = chips.indexOf(chip)
        chips.remove(chip)
        adapter.notifyItemRemoved(index)
    }

    fun getChips(): ArrayList<Chip> {
        return ArrayList(chips)
    }

    fun setSelectCallback(callback: ((Chip) -> Unit)?) {
        selectCallback = callback
    }

    fun setDeselectCallback(callback: ((Chip) -> Unit)?) {
        deselectCallback = callback
    }

    fun getSelectedPositions(): List<Int> {
        return adapter.getSelectedPositions()
    }

    fun getSelectedItems(): List<Chip> {
        return adapter.getSelectedItems()
    }

    fun select(chip: Chip) {
        adapter.select(chip)
    }

    fun select(position: Int) {
        adapter.select(position)
    }

    fun deselectAll() {
        adapter.deselectAll()
    }

    companion object {
        private val INVALID_RESOURCE = -1
        private val DEFAULT_EDGES_WIDTH = 16.toPx()
        private val DEFAULT_SPACE_BEFORE = 0.toPx()
        private val DEFAULT_SPACE_BETWEEN = 0.toPx()
        private val DEFAULT_SPACE_AFTER = 0.toPx()
    }
}