package com.ciandt.dragonfly.example.features.projectselection

import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.config.Benchmark
import com.ciandt.dragonfly.example.models.Project
import com.ciandt.dragonfly.example.models.Version
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
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


    private val nonEmptyList = ArrayList<Project>()


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        nonEmptyList.add(Project("1"))
        nonEmptyList.add(Project("2"))

        presenter = ProjectSelectionPresenter(interactor)
        presenter.attachView(view)
        presenter.start()
    }

    @After
    fun tearDown() {
        presenter.detachView()
        presenter.stop()
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

        presenter.loadProjects()

        argumentCaptor<(List<Project>) -> Unit>().apply {
            verify(interactor).loadProjects(capture(), any())
            firstValue.invoke(nonEmptyList)
        }

        verify(view).update(nonEmptyList)
    }

    @Test
    fun downloadWithoutVersion() {
        val project = Project("without-versions")

        presenter.download(project)

        verify(view).showUnavailable(project)
    }

    @Test
    fun downloadVersion() {
        val version = Version("not-downloaded")

        val project = Project("not-downloaded").apply {
            versions = arrayListOf(version)
        }

        presenter.download(project)

        argumentCaptor<() -> Unit>().apply {
            verify(view).confirmDownload(eq(project), capture())
            firstValue.invoke()
        }

        project.hasDownloadingVersion().shouldBeTrue()

        verify(view).update(project)

        verify(interactor).downloadVersion(any(), any(), eq(version), any())
    }

    @Test
    fun downloadVersionAndCancelConfirmation() {
        val version = Version("not-downloaded")

        val project = Project("not-downloaded").apply {
            versions = arrayListOf(version)
        }

        presenter.download(project)

        project.hasDownloadingVersion().shouldBeFalse()

        verify(view, never()).update(project)

        verify(interactor, never()).downloadVersion(any(), any(), eq(version), any())
    }

    @Test
    fun handleModelNotDownloadedWithError() {
        val version = Version("not-downloaded")

        val project = Project("not-downloaded").apply {
            versions = arrayListOf(version)
        }

        presenter.download(project)

        argumentCaptor<() -> Unit>().apply {
            verify(view).confirmDownload(eq(project), capture())
            firstValue.invoke()
        }

        project.hasDownloadingVersion().shouldBeTrue()

        verify(view).update(project)

        val exception = RuntimeException("test")

        argumentCaptor<(Exception) -> Unit>().apply {
            verify(interactor).downloadVersion(any(), any(), eq(version), capture())
            firstValue.invoke(exception)
        }

        project.hasDownloadingVersion().shouldBeFalse()

        verify(view).showDownloadError(exception)
    }

    @Test
    fun downloadWithVersionDownloading() {
        val project = Project("downloading").apply {
            versions = arrayListOf(Version("downloading", status = Version.STATUS_DOWNLOADING))
        }

        presenter.download(project)

        verify(view).showDownloading(project)
    }

    @Test
    fun runProjectWithVersionDownloaded() {
        val project = Project("downloaded").apply {
            versions = arrayListOf(Version("downloaded", status = Version.STATUS_DOWNLOADED))
        }

        val libraryModel = Model("downloaded/0")
        libraryModel.others.put(Benchmark.SHOW_BENCHMARK, project.showBenchmark)

        presenter.run(project)

        verify(view).run(libraryModel)
    }

    @Test
    fun projectObserverWithViewAttached() {
        val project = Project("test")

        argumentCaptor<(Project) -> Unit>().apply {
            verify(interactor).registerProjectObserver(capture())
            firstValue.invoke(project)
        }

        verify(view).update(eq(project))
    }

    @Test
    fun projectObserverWithoutViewAttached() {
        val project = Project("test")

        presenter.detachView()

        argumentCaptor<(Project) -> Unit>().apply {
            verify(interactor).registerProjectObserver(capture())
            firstValue.invoke(project)
        }

        verify(view, never()).update(eq(project))

        presenter.attachView(view)

        verify(view).update(eq(project))
    }

    @Test
    fun listObserverCalledBeforeLoad() {

        whenever(
                interactor.getTimestamp()
        ).thenReturn(1000L)

        var captured: ((Long) -> Unit)? = null
        argumentCaptor<(Long) -> Unit>().apply {
            verify(interactor).registerListObserver(capture())
            captured = firstValue
        }

        presenter.loadProjects()

        val changedAt = 500L
        captured?.invoke(changedAt)

        verify(view, never()).showSeeUpdates()
    }

    @Test
    fun listObserverCalledAfterLoad() {

        whenever(
                interactor.getTimestamp()
        ).thenReturn(1000L)


        var captured: ((Long) -> Unit)? = null
        argumentCaptor<(Long) -> Unit>().apply {
            verify(interactor).registerListObserver(capture())
            captured = firstValue
        }

        presenter.loadProjects()

        val changedAt = 2000L
        captured?.invoke(changedAt)

        verify(view).showSeeUpdates()
    }

    @Test
    fun listObserverCalledAfterLoadWithoutViewAttached() {

        whenever(
                interactor.getTimestamp()
        ).thenReturn(1000L)

        var captured: ((Long) -> Unit)? = null
        argumentCaptor<(Long) -> Unit>().apply {
            verify(interactor).registerListObserver(capture())
            captured = firstValue
        }

        presenter.loadProjects()

        presenter.detachView()

        val changedAt = 2000L
        captured?.invoke(changedAt)

        verify(view, never()).showSeeUpdates()

        presenter.attachView(view)

        verify(view).showSeeUpdates()
    }
}