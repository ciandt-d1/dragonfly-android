package com.ciandt.dragonfly.example.features.projectselection

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.ciandt.dragonfly.example.models.Project

class ProjectSelectionAdapter(var context: Context, var list: ArrayList<Project>, val itemClick: (Int, Project) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(ProjectSelectionViewHolder.getLayoutRes(), parent, false)
        return ProjectSelectionViewHolder(view, itemClick)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as ProjectSelectionViewHolder).bind(list[position])
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int, payloads: MutableList<Any>?) {
        var changed = false

        val first = payloads?.firstOrNull()
        if (first != null && first is Bundle) {
            changed = first.getBoolean("changed")
        }
        (holder as ProjectSelectionViewHolder).bind(list[position], changed)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemId(position: Int): Long {
        return list[position].hashCode().toLong()
    }
}