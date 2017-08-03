package com.ciandt.dragonfly.example.features.projectselection

import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.models.Project
import com.ciandt.dragonfly.example.models.Version
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.verify
import org.amshove.kluent.any
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@RunWith(JUnit4::class)
class ProjectSelectionPresenterTest {

    lateinit var presenter: ProjectSelectionPresenter

    @Mock
    lateinit var view: ProjectSelectionContract.View

    @Mock
    lateinit var interactor: ProjectSelectionContract.Interactor

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        presenter = ProjectSelectionPresenter(interactor)
        presenter.attachView(view)
    }

    @After
    fun tearDown() {
        presenter.detachView()
    }

    @Test
    fun loadProjects() {
        presenter.loadProjects()

        verify(view).showLoading()
        verify(interactor).loadProjects(any(), any())
    }

    @Test
    fun loadProjectsHandlingError() {

        val exception = RuntimeException("test")

        presenter.loadProjects()

        argumentCaptor<(Exception) -> Unit>().apply {
            verify(interactor).loadProjects(any(), capture())
            firstValue.invoke(exception)
        }

        verify(view).showError(exception)
    }

    @Test
    fun loadProjectsHandlingEmptyList() {

        val emptyList = ArrayList<Project>()

        presenter.loadProjects()

        argumentCaptor<(List<Project>) -> Unit>().apply {
            verify(interactor).loadProjects(capture(), any())
            firstValue.invoke(emptyList)
        }

        verify(view).showEmpty()
    }

    @Test
    fun loadProjectsHandlingNonEmptyList() {

        val nonEmptyList = ArrayList<Project>()
        nonEmptyList.add(Project("1"))
        nonEmptyList.add(Project("2"))

        presenter.loadProjects()

        argumentCaptor<(List<Project>) -> Unit>().apply {
            verify(interactor).loadProjects(capture(), any())
            firstValue.invoke(nonEmptyList)
        }

        verify(view).update(nonEmptyList)
    }

    @Test
    fun selectProjectWithoutVersion() {
        val project = Project("without-versions")

        presenter.selectProject(project)

        verify(view).showUnavailable(project)
    }

    @Test
    fun selectProjectWithVersionNotDownloaded() {
        val version = Version("not-downloaded")

        val project = Project("not-downloaded").apply {
            versions = arrayListOf(version)
        }

        presenter.selectProject(project)

        project.hasDownloadingVersion().shouldBeTrue()

        verify(view).update(project)

        verify(interactor).downloadVersion(any(), eq(version), any())
    }

    @Test
    fun handleModelNotDownloadedWithError() {
        val version = Version("not-downloaded")

        val project = Project("not-downloaded").apply {
            versions = arrayListOf(version)
        }

        presenter.selectProject(project)

        project.hasDownloadingVersion().shouldBeTrue()

        verify(view).update(project)

        val exception = RuntimeException("test")

        argumentCaptor<(Exception) -> Unit>().apply {
            verify(interactor).downloadVersion(any(), eq(version), capture())
            firstValue.invoke(exception)
        }

        project.hasDownloadingVersion().shouldBeFalse()

        verify(view).showDownloadError(exception)
    }

    @Test
    fun selectProjectWithVersionDownloading() {
        val project = Project("downloading").apply {
            versions = arrayListOf(Version("downloading", status = Version.STATUS_DOWNLOADING))
        }

        presenter.selectProject(project)

        verify(view).showDownloading(project)
    }

    @Test
    fun selectProjectWithVersionDownloaded() {
        val project = Project("downloaded").apply {
            versions = arrayListOf(Version("downloaded", status = Version.STATUS_DOWNLOADED))
        }

        val libraryModel = Model("downloaded/0")

        presenter.selectProject(project)

        verify(view).run(libraryModel)
    }
}