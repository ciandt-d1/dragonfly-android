package com.ciandt.dragonfly.example.models

data class Project(
        var id: String = "",
        var name: String = "",
        var description: String = "",
        var colors: List<String> = emptyList(),
        var versions: List<Version> = emptyList()
) {

    fun hasUpdate(): Boolean {
        return false
    }
}