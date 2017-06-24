package com.ciandt.dragonfly.example.components.chips

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.ciandt.dragonfly.example.R

class ChipAdapter(var context: Context, var list: ArrayList<out Chip>, val onClick: (chip: Chip) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val UNINITIALIZED_LAYOUT_RESOURCE = -1

    private var selectable = false
    private var allowsMultipleSelection = true
    private var layout = UNINITIALIZED_LAYOUT_RESOURCE

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {

        if (layout == UNINITIALIZED_LAYOUT_RESOURCE) {
            throw IllegalArgumentException("Invalid layout for item.")
        }

        val view = LayoutInflater.from(context).inflate(layout, parent, false)

        view.findViewById(R.id.chipButton) as Button? ?: throw IllegalArgumentException("Layout for item should contain a Button with id = button")

        return ChipViewHolder(this, view, selectable, onClick)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as ChipViewHolder).bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemId(position: Int): Long {
        return list[position].hashCode().toLong()
    }

    fun setSelectable(selectable: Boolean) {
        this.selectable = selectable
    }

    fun setAllowsMultipleSelection(allowsMultipleSelection: Boolean) {
        this.allowsMultipleSelection = allowsMultipleSelection

        if (!allowsMultipleSelection) {
            unselectAll()
        }
    }

    fun setChipLayout(layout: Int) {
        this.layout = layout
    }

    fun getSelected(): List<Chip> {
        val selected = ArrayList<Chip>()
        for (chip in list) {
            if (chip.isSelected()) {
                selected.add(chip)
            }
        }

        return selected
    }

    private fun unselectAll() {
        list.forEachIndexed { index, it ->
            if (it.isSelected()) {
                it.setSelected(false)
                notifyItemChanged(index)
            }
        }
    }

    private fun onChipSelected(chip: Chip) {
        clearOtherChipsSelection(chip)
    }

    private fun clearOtherChipsSelection(chip: Chip) {
        if (!allowsMultipleSelection) {
            list.forEachIndexed { index, it ->
                if (it !== chip && it.isSelected()) {
                    it.setSelected(false)
                    notifyItemChanged(index)
                }
            }
        }
    }

    companion object {
        private class ChipViewHolder(val adapter: ChipAdapter, itemView: View, val selectable: Boolean, val onClick: (chip: Chip) -> Unit) : RecyclerView.ViewHolder(itemView) {

            fun bind(chip: Chip) = with(itemView.findViewById(R.id.chipButton) as Button) {

                text = chip.getText()
                isActivated = if (selectable) chip.isSelected() else false

                setOnClickListener {
                    if (selectable) {
                        isActivated = !isActivated
                        chip.setSelected(isActivated)

                        if (isActivated) {
                            adapter.onChipSelected(chip)
                        }
                    }
                    onClick(chip)
                }
            }
        }
    }
}
