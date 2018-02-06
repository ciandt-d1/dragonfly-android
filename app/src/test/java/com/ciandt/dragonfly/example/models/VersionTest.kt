package com.ciandt.dragonfly.example.models

import com.ciandt.dragonfly.data.model.Model
import org.amshove.kluent.shouldEqual
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertTrue

@RunWith(JUnit4::class)
class VersionTest {

    @Test
    fun statusDefaultIsNotDownloaded() {
        val version = Version()
        assertTrue {
            version.isNotDownloaded()
        }
    }

    @Test
    fun toLibraryModel() {

        val version = Version(
                "project-id",
                1,
                88481067,
                299,
                128,
                128f,
                "Mul",
                "final_result",
                "Display Name",
                "false",
                "gs://v1/projects/project-id/1.zip",
                1489327402,
                "project-id/1/model.pb",
                "project-id/1/model.txt"
        )

        val model = Model("project-id/1")
                .setVersion(1)
                .setSizeInBytes(88481067)
                .setInputSize(299)
                .setImageMean(128)
                .setImageStd(128f)
                .setInputName("Mul")
                .setOutputNames("final_result")
                .setOutputDisplayNames("Display Name")
                .setClosedSet("false")
                .setModelPath("project-id/1/model.pb")
                .setLabelFilesPaths("project-id/1/model.txt")

        version.toLibraryModel().shouldEqual(model)
    }
}