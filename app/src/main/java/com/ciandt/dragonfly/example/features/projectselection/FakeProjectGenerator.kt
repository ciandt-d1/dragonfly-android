package com.ciandt.dragonfly.example.features.projectselection

import com.ciandt.dragonfly.example.models.Project
import com.ciandt.dragonfly.example.models.Version

// REMOVE ON SMTC-1489
object FakeProjectGenerator {

    fun getProjects(): List<Project> {

        val list = arrayListOf<Project>()

        list.add(getFlowers())
        list.add(getTensorflowDemo())
        list.add(getHP())

        return list
    }

    private fun getFlowers(): Project {

        val project = Project(
                "flowers",
                "Flowers",
                "This model classifies 130 plants",
                listOf("#9BCE4F", "#228B22")
        )

        val version = Version(
                "flowers",
                1,
                88481067L,
                299,
                128,
                128f,
                "Mul",
                "final_result",
                "",
                1L,
                "file:///android_asset/models/flowers/model.pb",
                "file:///android_asset/models/flowers/labels.txt",
                Version.STATUS_DOWNLOADED
        )

        project.versions = listOf(version)
        return project
    }

    private fun getTensorflowDemo(): Project {

        val project = Project(
                "tf-demo",
                "TF Demo",
                "Simple model for demonstration",
                listOf("#8E78FD", "#826BF2")
        )

        val version = Version(
                "tf-demo",
                1,
                53884595L,
                224,
                117,
                1f,
                "input",
                "output",
                "",
                1L,
                "file:///android_asset/models/demo/model.pb",
                "file:///android_asset/models/demo/labels.txt",
                Version.STATUS_DOWNLOADED
        )

        project.versions = listOf(version)
        return project
    }

    private fun getHP(): Project {

        val project = Project(
                "sorting-hat",
                "Sorting Hat",
                "Which Hogwarts house would you be sorted into?",
                listOf("#FDD736", "#990000")
        )

        val version = Version(
                "sorting-hat",
                1,
                87164854L,
                299,
                128,
                128f,
                "Mul",
                "final_result",
                "",
                1L,
                "file:///android_asset/models/hp/model.pb",
                "file:///android_asset/models/hp/labels.txt",
                Version.STATUS_DOWNLOADED
        )

        project.versions = listOf(version)
        return project
    }
}