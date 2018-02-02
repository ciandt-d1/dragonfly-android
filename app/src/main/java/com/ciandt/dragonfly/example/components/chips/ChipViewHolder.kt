package com.ciandt.dragonfly.example.components.chips

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import com.ciandt.dragonfly.example.R

class ChipViewHolder(
        itemView: View,
        private val onClick: (chip: Chip, activated: Boolean) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    fun bind(chip: Chip, selectable: Boolean = false, selected: Boolean = false) = with(itemView.findViewById<Button>(R.id.chipButton)) {

        isClickable =  selectable

        text = chip.getText()
        isActivated = selected

        if (selectable) {
            setOnClickListener {
                isActivated = !isActivated
                onClick(chip, isActivated)
            }
        } else {
            setOnClickListener(null)
        }
    }
}