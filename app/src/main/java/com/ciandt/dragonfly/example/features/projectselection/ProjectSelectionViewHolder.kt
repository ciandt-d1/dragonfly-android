package com.ciandt.dragonfly.example.features.projectselection

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.components.buttons.DownloadButton
import com.ciandt.dragonfly.example.helpers.ColorHelper
import com.ciandt.dragonfly.example.helpers.SizeHelper
import com.ciandt.dragonfly.example.infrastructure.extensions.makeGone
import com.ciandt.dragonfly.example.infrastructure.extensions.makeVisible
import com.ciandt.dragonfly.example.models.Project
import com.ciandt.dragonfly.example.models.Version
import kotlinx.android.synthetic.main.item_project_selection.view.*
import java.text.DecimalFormat

class ProjectSelectionViewHolder(itemView: View, val itemClick: (Int, Project) -> Unit) : RecyclerView.ViewHolder(itemView) {

    fun bind(item: Project, animate: Boolean = false) = with(itemView) {

        name.text = item.name

        description.text = item.description

        if (item.colors.size >= 2) {
            val gradient = ColorHelper.toGradient(item.colors[0], item.colors[1])
            container.background = gradient
        } else if (item.colors.size == 1) {
            val color = ColorHelper.parseColor(item.colors[0])
            container.setBackgroundColor(color)
        }

        if (!item.hasAnyVersion()) {

            explore.makeGone()
            download.makeVisible()

            val text = resources.getString(R.string.project_selection_item_unavailable)
            download.setState(DownloadButton.State.Start(null, text), animate)

        } else {

            val lastVersion = item.getLastVersion()!!

            val format = resources.getString(R.string.project_selection_item_info, lastVersion.version, SizeHelper.toReadable(lastVersion.size, format = DecimalFormat("#.##")))
            info.text = format

            explore.makeGone()
            download.makeGone()

            if (item.hasDownloadedVersion()) {

                val text = resources.getString(R.string.project_selection_item_downloaded)

                val setExplore = {
                    explore.makeVisible()
                    explore.setState(DownloadButton.State.Done(text), false)
                }

                if (animate && lastVersion.isDownloaded()) {

                    download.makeVisible()
                    download.setState(DownloadButton.State.Done(text), true, {
                        download.makeGone()

                        setExplore()
                    })

                } else {
                    setExplore()
                }
            }

            when (lastVersion.status) {
                Version.STATUS_DOWNLOADED -> {
                }

                Version.STATUS_DOWNLOADING -> {
                    download.makeVisible()
                    download.setState(DownloadButton.State.Progress(), animate)
                }

                else -> {
                    val drawable = ContextCompat.getDrawable(context, if (item.hasUpdate()) R.drawable.ic_update else R.drawable.ic_download)
                    val text = resources.getString(if (item.hasUpdate()) R.string.project_selection_item_update else R.string.project_selection_item_download)
                    download.makeVisible()
                    download.setState(DownloadButton.State.Start(drawable, text), animate)
                }
            }

        }

        explore.setOnClickListener {
            itemClick(BUTTON_EXPLORE, item)
        }

        download.setOnClickListener {
            itemClick(BUTTON_DOWNLOAD, item)
        }
    }

    companion object {

        val BUTTON_EXPLORE = 1
        val BUTTON_DOWNLOAD = 2

        fun getLayoutRes(): Int = R.layout.item_project_selection
    }
}