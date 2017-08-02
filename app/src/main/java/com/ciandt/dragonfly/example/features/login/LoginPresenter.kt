package com.ciandt.dragonfly.example.features.login

import com.ciandt.dragonfly.example.shared.BasePresenter
import com.google.android.gms.auth.api.signin.GoogleSignInResult

class LoginPresenter(val interactor: LoginContract.Interactor) : BasePresenter<LoginContract.View>(), LoginContract.Presenter {

    override fun signInWithGoogle() {
        view?.showLoading()
        view?.startSignInWithGoogle()
    }

    override fun checkSignInWithGoogle(result: GoogleSignInResult) {
        if (result.isSuccess && result.signInAccount != null) {
            signInWithGoogleOnFirebase(result.signInAccount!!.idToken ?: "")

        } else {
            view?.showError(RuntimeException("Google Sign In failed"))
        }
    }

    override fun signInWithGoogleFailed(errorCode: Int, errorMessage: String?) {
        view?.showError(RuntimeException("Google Sign In - ConnectionFailed: $errorCode - $errorMessage"))
    }

    override fun signInWithGoogleCanceled(networkAvailable: Boolean) {
        if (!networkAvailable) {
            view?.showError(RuntimeException("No network available"))
        } else {
            view?.cancel()
        }
    }

    private fun signInWithGoogleOnFirebase(token: String) = interactor.signInWithGoogle(token, onSuccess = {

        view?.goToMain()

    }, onFailure = { exception ->

        view?.showError(RuntimeException("Firebase Sign In failed", exception))

    })

}