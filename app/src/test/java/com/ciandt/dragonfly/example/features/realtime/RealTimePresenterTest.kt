package com.ciandt.dragonfly.example.features.realtime

import com.ciandt.dragonfly.example.config.PreferenceKeys
import com.ciandt.dragonfly.example.infrastructure.PreferencesRepository
import com.nhaarman.mockito_kotlin.atLeastOnce
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.amshove.kluent.any
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@RunWith(JUnit4::class)
class RealTimePresenterTest {

    lateinit var presenter: RealTimePresenter

    @Mock
    lateinit var view: RealTimeContract.View

    @Mock
    lateinit var preferencesRepository: PreferencesRepository

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        presenter = RealTimePresenter(preferencesRepository)
    }

    @After
    fun tearDown() {
        presenter.detachView()
    }

    @Test
    fun attachView() {

        whenever(
                preferencesRepository.getBoolean(eq(PreferenceKeys.REAL_TIME_PERMISSIONS_PERMANENTLY_DENIED), any())
        ).thenReturn(false)

        presenter.attachView(view)

        verify(view).checkRealTimeRequiredPermissions()
    }

    @Test
    fun attachViewWithPermissionsDenied() {

        whenever(
                preferencesRepository.getBoolean(eq(PreferenceKeys.REAL_TIME_PERMISSIONS_PERMANENTLY_DENIED), any())
        ).thenReturn(true)

        presenter.attachView(view)

        verify(view).showPermissionsRequiredAlert(any(), any(), any())
    }

    @Test
    fun onRealTimePermissionsDenied() {

        whenever(
                preferencesRepository.getBoolean(eq(PreferenceKeys.REAL_TIME_PERMISSIONS_PERMANENTLY_DENIED), any())
        ).thenReturn(false)

        presenter.attachView(view)

        verify(view).checkRealTimeRequiredPermissions()

        presenter.onRealTimePermissionsDenied(false)

        verify(preferencesRepository, never()).putBoolean(PreferenceKeys.REAL_TIME_PERMISSIONS_PERMANENTLY_DENIED, true)

        verify(view, atLeastOnce()).checkRealTimeRequiredPermissions()
    }

    @Test
    fun onRealTimePermissionsDeniedPermanently() {

        whenever(
                preferencesRepository.getBoolean(eq(PreferenceKeys.REAL_TIME_PERMISSIONS_PERMANENTLY_DENIED), any())
        ).thenReturn(false)

        presenter.attachView(view)

        verify(view).checkRealTimeRequiredPermissions()

        presenter.onRealTimePermissionsDenied(true)

        verify(preferencesRepository).putBoolean(PreferenceKeys.REAL_TIME_PERMISSIONS_PERMANENTLY_DENIED, true)

        verify(view).showPermissionsRequiredAlert(any(), any(), any())
    }

    @Test
    fun onRealTimePermissionsGranted() {

        whenever(
                preferencesRepository.getBoolean(eq(PreferenceKeys.REAL_TIME_PERMISSIONS_PERMANENTLY_DENIED), any())
        ).thenReturn(false)

        presenter.attachView(view)

        verify(view).checkRealTimeRequiredPermissions()

        presenter.onRealTimePermissionsGranted()

        verify(preferencesRepository).remove(PreferenceKeys.REAL_TIME_PERMISSIONS_PERMANENTLY_DENIED)

        verify(view).startRealTimeClassification()
    }
}