package com.ciandt.dragonfly.example.components.chips

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class SpaceItemDecoration(
        val spaceBefore: Int = 0,
        val spaceBetween: Int = 0,
        val spaceAfter: Int = 0
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {

        outRect?.left = spaceBetween
        outRect?.right = 0
        outRect?.bottom = 0
        outRect?.top = 0

        parent?.let {
            val position = it.getChildAdapterPosition(view)
            val lastPosition = if (it.adapter.itemCount == 0) 0 else it.adapter.itemCount - 1

            when (position) {
                0 -> outRect?.left = spaceBefore
                lastPosition -> outRect?.right = spaceAfter
            }
        }
    }
}