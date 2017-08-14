package com.ciandt.dragonfly.example.models

import org.amshove.kluent.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.*

@RunWith(JUnit4::class)
class ProjectTest {

    @Test
    fun versionsIsEmptyByDefault() {
        with(Project()) {
            versions.isEmpty().shouldBeTrue()

            hasAnyVersion().shouldBeFalse()

            hasDownloadingVersion().shouldBeFalse()

            hasDownloadedVersion().shouldBeFalse()
        }
    }

    @Test
    fun versionsNonEmpty() {
        val project = Project(versions = arrayListOf(
                Version("project-id", 1),
                Version("project-id", 2)
        ))

        with(project) {
            versions.isEmpty().shouldBeFalse()

            hasAnyVersion().shouldBeTrue()

            hasDownloadingVersion().shouldBeFalse()

            hasDownloadedVersion().shouldBeFalse()
        }
    }

    @Test
    fun lastVersionWithoutVersions() {
        Project().getLastVersion().shouldBeNull()
    }

    @Test
    fun lastVersionWithVersions() {
        val lastVersion = Version("project-id", 2)
        val versions = arrayListOf(
                Version("project-id", 1),
                lastVersion
        )
        Project(versions = versions).getLastVersion().shouldBe(lastVersion)
    }

    @Test
    fun lastVersionWithUnorderedVersions() {
        val lastVersion = Version("project-id", 2)
        val versions = arrayListOf(
                lastVersion,
                Version("project-id", 1)
        )
        Project(versions = versions).getLastVersion().shouldBe(lastVersion)
    }

    @Test
    fun lastDownloadVersionWithoutVersions() {
        Project().getLastDownloadedVersion().shouldBeNull()
    }

    @Test
    fun lastDownloadVersionWithoutDownloadedVersions() {
        val versions = arrayListOf(
                Version("project-id", 1),
                Version("project-id", 2)
        )
        Project(versions = versions).getLastDownloadedVersion().shouldBeNull()
    }

    @Test
    fun lastDownloadVersionWithDownloadedVersions() {
        val lastDownloaded = Version("project-id", 2, status = Version.STATUS_DOWNLOADED)
        val versions = arrayListOf(
                Version("project-id", 1),
                lastDownloaded
        )
        Project(versions = versions).getLastDownloadedVersion().shouldEqual(lastDownloaded)
    }

    @Test
    fun lastDownloadVersionWithManyDownloadedVersions() {
        val lastDownloaded = Version("project-id", 3, status = Version.STATUS_DOWNLOADED)
        val versions = arrayListOf(
                Version("project-id", 1),
                Version("project-id", 2),
                lastDownloaded
        )
        Project(versions = versions).getLastDownloadedVersion().shouldEqual(lastDownloaded)
    }

    @Test
    fun lastDownloadVersionWithUnorderedDownloadedVersions() {
        val lastDownloaded = Version("project-id", 5, status = Version.STATUS_DOWNLOADED)
        val versions = arrayListOf(
                lastDownloaded,
                Version("project-id", 1),
                Version("project-id", 2, status = Version.STATUS_DOWNLOADED),
                Version("project-id", 3, status = Version.STATUS_DOWNLOADING),
                Version("project-id", 4, status = Version.STATUS_DOWNLOADED)
        )
        Project(versions = versions).getLastDownloadedVersion().shouldEqual(lastDownloaded)
    }

    @Test
    fun allTypesOfVersions() {
        val notDownloaded = Version("project-id", 1, status = Version.STATUS_NOT_DOWNLOADED)
        val lastDownloading = Version("project-id", 2, status = Version.STATUS_DOWNLOADING)
        val lastDownloaded = Version("project-id", 3, status = Version.STATUS_DOWNLOADED)
        val last = Version("project-id", 4, status = Version.STATUS_NOT_DOWNLOADED)

        val versions = arrayListOf(
                notDownloaded,
                lastDownloading,
                lastDownloaded,
                last
        )

        Collections.shuffle(versions)

        val project = Project(versions = versions)

        project.hasAnyVersion().shouldBeTrue()
        project.hasDownloadingVersion().shouldBeTrue()
        project.hasDownloadedVersion().shouldBeTrue()

        project.getLastVersion().shouldEqual(last)
        project.getLastDownloadedVersion().shouldEqual(lastDownloaded)
    }

    @Test
    fun hasUpdateWhenThereIsNoDownloadedVersion() {
        val versions = arrayListOf(
                Version("project-id", 1),
                Version("project-id", 2),
                Version("project-id", 3)
        )
        Project(versions = versions).hasUpdate().shouldBeFalse()
    }

    @Test
    fun hasUpdateWhenLastVersionIsDownloaded() {
        val versions = arrayListOf(
                Version("project-id", 1),
                Version("project-id", 2),
                Version("project-id", 3, status = Version.STATUS_DOWNLOADED)
        )
        Project(versions = versions).hasUpdate().shouldBeFalse()
    }

    @Test
    fun hasUpdateWhenLastVersionIsNotDownloaded() {
        val lastVersion = Version("project-id", 3)
        val versions = arrayListOf(
                Version("project-id", 1),
                Version("project-id", 2, status = Version.STATUS_DOWNLOADED),
                lastVersion
        )

        with(Project(versions = versions)) {
            hasUpdate().shouldBeTrue()
            getLastVersion().shouldBe(lastVersion)
            lastVersion.isNotDownloaded().shouldBeTrue()
        }
    }

    @Test
    fun hasUpdateWhenLastVersionsAreNotDownloaded() {
        val lastVersion = Version("project-id", 4)
        val versions = arrayListOf(
                Version("project-id", 1),
                Version("project-id", 2, status = Version.STATUS_DOWNLOADED),
                lastVersion,
                Version("project-id", 3)
        )

        with(Project(versions = versions)) {
            hasUpdate().shouldBeTrue()
            getLastVersion().shouldBe(lastVersion)
            lastVersion.isNotDownloaded().shouldBeTrue()
        }
    }
}