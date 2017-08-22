package com.ciandt.dragonfly.example.features.feedback

import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.features.feedback.model.BenchmarkResult
import com.ciandt.dragonfly.lens.data.DragonflyClassificationInput
import com.ciandt.dragonfly.tensorflow.Classifier
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@RunWith(JUnit4::class)
class FeedbackPresenterTest {

    lateinit var presenter: FeedbackPresenter

    @Mock
    lateinit var view: FeedbackContract.View

    @Mock
    lateinit var model: Model

    @Mock
    lateinit var classificationInput: DragonflyClassificationInput

    @Mock
    lateinit var feedbackSaverInteractor: FeedbackContract.SaverInteractor

    @Mock
    lateinit var saveImageToGalleryInteractor: SaveImageToGalleryContract.Interactor

    @Mock
    lateinit var benchmarkInteractor: BenchmarkContract.Interactor

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        presenter = FeedbackPresenter(model, classificationInput, "userId",
                feedbackSaverInteractor,
                saveImageToGalleryInteractor,
                benchmarkInteractor)

        presenter.attachView(view)
    }

    @After
    fun tearDown() {
        presenter.detachView()
    }

    @Test
    fun benchmark() {
        presenter.benchmark()

        verify(view).showBenchmarkLoading()

        verify(benchmarkInteractor).benchmark(eq(classificationInput), any(), any())
    }

    @Test
    fun benchmarkHandlingError() {

        val exception = RuntimeException("test")

        presenter.benchmark()

        argumentCaptor<(Exception) -> Unit>().apply {
            verify(benchmarkInteractor).benchmark(eq(classificationInput), any(), capture())
            firstValue.invoke(exception)
        }

        verify(view).showBenchmarkError(eq(exception))
    }

    @Test
    fun benchmarkHandlingEmptyResults() {

        val emptyResult = BenchmarkResult()

        presenter.benchmark()

        argumentCaptor<(BenchmarkResult) -> Unit>().apply {
            verify(benchmarkInteractor).benchmark(eq(classificationInput), capture(), any())
            firstValue.invoke(emptyResult)
        }

        verify(view).showBenchmarkEmpty()
    }

    @Test
    fun benchmarkHandlingNonEmptyResultButWithoutClassifications() {

        val nonEmptyResult = BenchmarkResult()
        nonEmptyResult.addBenchmarkService(BenchmarkResult.BenchmarkService("service-a", "Service A", mutableListOf()))

        presenter.benchmark()

        argumentCaptor<(BenchmarkResult) -> Unit>().apply {
            verify(benchmarkInteractor).benchmark(eq(classificationInput), capture(), any())
            firstValue.invoke(nonEmptyResult)
        }

        verify(view).showBenchmarkEmpty()
    }

    @Test
    fun benchmarkHandlingNonEmptyResult() {

        val nonEmptyResult = BenchmarkResult()
        nonEmptyResult.addBenchmarkService(BenchmarkResult.BenchmarkService("service-a", "Service A", mutableListOf(
                Classifier.Classification("1", "alpha", 0.6f, null),
                Classifier.Classification("2", "beta", 0.3f, null)
        )))

        presenter.benchmark()

        argumentCaptor<(BenchmarkResult) -> Unit>().apply {
            verify(benchmarkInteractor).benchmark(eq(classificationInput), capture(), any())
            firstValue.invoke(nonEmptyResult)
        }

        verify(view).showBenchmarkResult(eq(nonEmptyResult))
    }
}