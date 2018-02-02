package com.ciandt.dragonfly.example.components.chips

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import com.ciandt.dragonfly.example.R

class ChipAdapter(
        var context: Context,
        var list: ArrayList<out Chip>,
        val onClick: (chip: Chip, activated: Boolean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectable = false
    private var multipleSelection = false
    private var layout = UNINITIALIZED_LAYOUT_RESOURCE

    private val selectedPositions = ArrayList<Int>()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {

        if (layout == UNINITIALIZED_LAYOUT_RESOURCE) {
            throw IllegalArgumentException("Invalid layout for item.")
        }

        val view = LayoutInflater.from(context).inflate(layout, parent, false)

        view.findViewById<Button?>(R.id.chipButton) ?:
                throw IllegalArgumentException("Layout for item should contain a Button with id = R.id.chipButton")

        return ChipViewHolder(view, selectable) { chip, activated ->

            val index = list.indexOf(chip)

            val changes = ArrayList<Int>()
            changes.add(index)

            if (activated) {

                if (!multipleSelection) {
                    changes.addAll(selectedPositions)
                    selectedPositions.clear()
                }

                selectedPositions.add(index)

            } else {

                selectedPositions.remove(index)
            }

            changes.forEach {
                notifyItemChanged(it)
            }

            onClick.invoke(chip, activated)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as ChipViewHolder).bind(list[position], selectedPositions.contains(position))
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

    fun setMultipleSelection(multipleSelection: Boolean) {
        this.multipleSelection = multipleSelection
    }

    fun setChipLayout(layout: Int) {
        this.layout = layout
    }

    fun getSelectedPositions(): List<Int> {
        return selectedPositions
    }

    fun getSelectedItems(): List<Chip> {
        val items = ArrayList<Chip>()
        selectedPositions.forEach {
            items.add(list[it])
        }
        return items
    }

    fun select(chip: Chip) {
        val position = list.indexOf(chip)
        if (position >= 0) {
            selectedPositions.add(position)
            notifyItemChanged(position)
        }
    }

    companion object {
        private val UNINITIALIZED_LAYOUT_RESOURCE = -1
    }
}