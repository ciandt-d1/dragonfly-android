package com.ciandt.dragonfly.example.features.modelselection

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import com.ciandt.dragonfly.data.Model
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.helpers.ColorHelper
import com.ciandt.dragonfly.example.helpers.SizeHelper
import kotlinx.android.synthetic.main.item_model_selection.view.*
import java.text.DecimalFormat

class ModelSelectionViewHolder(itemView: View, val itemClick: (Model) -> Unit) : RecyclerView.ViewHolder(itemView) {

    fun bind(item: Model) = with(itemView) {

        name.text = item.name

        description.text = item.description

        val format = resources.getString(R.string.model_selection_item_info, item.version, SizeHelper.toReadable(item.size, format = DecimalFormat("#.##")))
        info.text = format

        if (item.colors.size >= 2) {
            val gradient = ColorHelper.toGradient(item.colors[0], item.colors[1])
            container.background = gradient
        }

        with(download) {
            when (item.status) {
                Model.STATUS_DOWNLOADED -> {
                    text = resources.getString(R.string.model_selection_item_downloaded)
                    setTextColor(ContextCompat.getColor(download.context, R.color.model_selection_item_downloaded))
                    setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_downloaded_model, 0)
                }
                Model.STATUS_DOWNLOADING -> {
                    text = resources.getString(R.string.model_selection_item_downloading)
                    setTextColor(ContextCompat.getColor(download.context, R.color.model_selection_item_downloading))
                    setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }
                else -> {
                    text = resources.getString(R.string.model_selection_item_download)
                    setTextColor(ContextCompat.getColor(download.context, R.color.model_selection_item_download))
                    setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_download_model, 0)
                }
            }

            setOnClickListener {
                itemClick(item)
            }
        }

        setOnClickListener {
            itemClick(item)
        }

        if (resources.configuration.fontScale > 1) {

            icon.visibility = View.GONE

            download.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            download.compoundDrawablePadding = 0
        }
    }

    companion object {
        fun getLayoutRes(): Int = R.layout.item_model_selection
    }
}