package com.ciandt.dragonfly.example.components.chips

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import com.ciandt.dragonfly.example.R

class ChipViewHolder(itemView: View, val selectable: Boolean, val onClick: (chip: Chip, activated: Boolean) -> Unit) : RecyclerView.ViewHolder(itemView) {

    fun bind(chip: Chip, selected: Boolean = false) = with(itemView.findViewById(R.id.chipButton) as Button) {

        text = chip.getText()
        isActivated = if (selectable) selected else false

        setOnClickListener {
            if (selectable) {
                isActivated = !isActivated
            }
            onClick(chip, isActivated)
        }
    }
}