package com.ciandt.dragonfly.example.features.modelselection

import com.ciandt.dragonfly.data.Model
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.verify
import org.amshove.kluent.any
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertTrue

@RunWith(JUnit4::class)
class ModelSelectionPresenterTest {

    lateinit var presenter: ModelSelectionPresenter

    @Mock
    lateinit var view: ModelSelectionContract.View

    @Mock
    lateinit var interactor: ModelSelectionContract.Interactor

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        presenter = ModelSelectionPresenter(interactor)
        presenter.attachView(view)
    }

    @After
    fun tearDown() {
        presenter.detachView()
    }

    @Test
    fun loadModels() {
        presenter.loadModels()

        verify(view).showLoading()
        verify(interactor).loadModels(any(), any())
    }

    @Test
    fun loadModelsHandlingError() {

        val exception = RuntimeException("test")

        presenter.loadModels()

        argumentCaptor<(Exception) -> Unit>().apply {
            verify(interactor).loadModels(any(), capture())
            firstValue.invoke(exception)
        }

        verify(view).showError(exception)
    }

    @Test
    fun loadModelsHandlingEmptyList() {

        val emptyList = ArrayList<Model>()

        presenter.loadModels()

        argumentCaptor<(List<Model>) -> Unit>().apply {
            verify(interactor).loadModels(capture(), any())
            firstValue.invoke(emptyList)
        }

        verify(view).showEmpty()
    }

    @Test
    fun loadModelsHandlingNonEmptyList() {

        val nonEmptyList = ArrayList<Model>()
        nonEmptyList.add(Model("1"))
        nonEmptyList.add(Model("2"))

        presenter.loadModels()

        argumentCaptor<(List<Model>) -> Unit>().apply {
            verify(interactor).loadModels(capture(), any())
            firstValue.invoke(nonEmptyList)
        }

        verify(view).update(nonEmptyList)
    }

    @Test
    fun selectModel() {
        val model = Model("not-downloaded")

        presenter.selectModel(model)

        assertTrue {
            model.isDownloading
        }

        verify(view).update(model)
    }

    @Test
    fun selectModelDownloaded() {
        val model = Model("downloaded")
        model.status = Model.STATUS_DOWNLOADED

        presenter.selectModel(model)

        verify(view).run(model)
    }

    @Test
    fun selectModelDownloading() {
        val model = Model("downloading")
        model.status = Model.STATUS_DOWNLOADING

        presenter.selectModel(model)

        verify(view).showDownloading(model)
    }
}