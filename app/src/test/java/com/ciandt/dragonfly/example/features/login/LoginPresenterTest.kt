package com.ciandt.dragonfly.example.features.login

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.firebase.auth.FirebaseUser
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@RunWith(JUnit4::class)
class LoginPresenterTest {

    lateinit var presenter: LoginPresenter

    @Mock
    lateinit var view: LoginContract.View

    @Mock
    lateinit var interactor: LoginContract.Interactor

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        presenter = LoginPresenter(interactor)
        presenter.attachView(view)
    }

    @After
    fun tearDown() {
        presenter.detachView()
    }

    @Test
    fun startSignInWithGoogle() {

        presenter.signInWithGoogle()

        verify(view).showLoading()
        verify(view).startSignInWithGoogle()
    }

    @Test
    fun signInWithGoogleFailed() {

        presenter.signInWithGoogleFailed(1, "error")

        verify(view).showError(any())
    }

    @Test
    fun signInWithGoogleCanceled() {

        presenter.signInWithGoogleCanceled(true)

        verify(view).cancel()
    }

    @Test
    fun signInWithGoogleOffline() {

        presenter.signInWithGoogleCanceled(false)

        verify(view).showError(any())
    }

    @Test
    fun successSignInWithGoogle() {

        val account = mock<GoogleSignInAccount> {
            on { idToken } doReturn "1"
        }

        val result = mock<GoogleSignInResult> {
            on { isSuccess } doReturn true
            on { signInAccount } doReturn account
        }

        val user = mock<FirebaseUser> {
        }

        presenter.checkSignInWithGoogle(result)

        argumentCaptor<(FirebaseUser) -> Unit>().apply {
            verify(interactor).signInWithGoogle(eq("1"), capture(), any())
            firstValue.invoke(user)
        }

        verify(view).goToMain()
    }

    @Test
    fun successSignInWithGoogleButWithFirebaseError() {

        val account = mock<GoogleSignInAccount> {
            on { idToken } doReturn "1"
        }

        val result = mock<GoogleSignInResult> {
            on { isSuccess } doReturn true
            on { signInAccount } doReturn account
        }

        presenter.checkSignInWithGoogle(result)

        argumentCaptor<(Exception?) -> Unit>().apply {
            verify(interactor).signInWithGoogle(eq("1"), any(), capture())
            firstValue.invoke(RuntimeException("error"))
        }

        verify(view).showError(any())
    }

    @Test
    fun errorSignInWithGoogle() {

        val account = mock<GoogleSignInAccount> {
        }

        val result = mock<GoogleSignInResult> {
            on { isSuccess } doReturn false
            on { signInAccount } doReturn account
        }

        presenter.checkSignInWithGoogle(result)

        verify(interactor, never()).signInWithGoogle(any(), any(), any())

        verify(view).showError(any())
    }

}