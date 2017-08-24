package com.ciandt.dragonfly.example.features.projectselection

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.helpers.ColorHelper
import com.ciandt.dragonfly.example.helpers.SizeHelper
import com.ciandt.dragonfly.example.models.Project
import com.ciandt.dragonfly.example.models.Version
import kotlinx.android.synthetic.main.item_project_selection.view.*
import java.text.DecimalFormat

class ProjectSelectionViewHolder(itemView: View, val itemClick: (Project) -> Unit) : RecyclerView.ViewHolder(itemView) {

    fun bind(item: Project) = with(itemView) {

        name.text = item.name

        description.text = item.description

        if (item.colors.size >= 2) {
            val gradient = ColorHelper.toGradient(item.colors[0], item.colors[1])
            container.background = gradient
        } else if (item.colors.size == 1) {
            val color = ColorHelper.parseColor(item.colors[0])
            container.setBackgroundColor(color)
        }

        if (item.hasAnyVersion()) {

            val lastVersion = item.getLastVersion()!!

            val format = resources.getString(R.string.project_selection_item_info, lastVersion.version, SizeHelper.toReadable(lastVersion.size, format = DecimalFormat("#.##")))
            info.text = format

            with(download) {
                when (lastVersion.status) {
                    Version.STATUS_DOWNLOADED -> {
                        text = resources.getString(R.string.project_selection_item_downloaded)
                        setTextColor(ContextCompat.getColor(context, R.color.project_selection_item_downloaded))
                    }
                    Version.STATUS_DOWNLOADING -> {
                        text = resources.getString(R.string.project_selection_item_downloading)
                        setTextColor(ContextCompat.getColor(context, R.color.project_selection_item_downloading))
                    }
                    else -> {
                        text = resources.getString(if (item.hasUpdate()) R.string.project_selection_item_update else R.string.project_selection_item_download)
                        setTextColor(ContextCompat.getColor(context, R.color.project_selection_item_download))
                    }
                }

                setOnClickListener {
                    itemClick(item)
                }
            }

        } else {
            download.apply {
                text = resources.getString(R.string.project_selection_item_unavailable)
                setTextColor(ContextCompat.getColor(context, R.color.project_selection_item_unavailable))
                setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
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
        fun getLayoutRes(): Int = R.layout.item_project_selection
    }
}