package com.ciandt.dragonfly.example.features.login

import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger
import com.ciandt.dragonfly.example.shared.BasePresenter
import com.google.android.gms.auth.api.signin.GoogleSignInResult

/**
 * Created by iluz on 6/20/17.
 */
class LoginPresenter(val interactor: LoginContract.Interactor) : BasePresenter<LoginContract.View>(), LoginContract.Presenter {

    override fun signInWithGoogle() {
        view?.showLoading()
        view?.startSignInWithGoogle()
    }

    override fun checkSignInWithGoogle(result: GoogleSignInResult) {
        if (result.isSuccess && result.signInAccount != null) {
            signInWithGoogleOnFirebase(result.signInAccount!!.idToken ?: "")

        } else {
            DragonflyLogger.warn(LOG_TAG, "getSignInResultFromIntent: Google Sign In failed")
            view?.showError()
        }
    }

    override fun signInWithGoogleFailed(errorCode: Int, errorMessage: String?) {
        DragonflyLogger.warn(LOG_TAG, "onConnectionFailed: $errorCode - $errorMessage")
        view?.showError()
    }

    override fun signInWithGoogleCanceled(networkAvailable: Boolean) {
        if (!networkAvailable) {
            view?.showError()
        } else {
            view?.cancel()
        }
    }

    private fun signInWithGoogleOnFirebase(token: String) = interactor.signInWithGoogle(token, onSuccess = {

        DragonflyLogger.debug(LOG_TAG, "signInWithGoogle: success")
        view?.goToMain()

    }, onFailure = { exception ->

        DragonflyLogger.warn(LOG_TAG, "signInAnonymously: failure $exception")
        view?.showError()

    })

    companion object {
        private val LOG_TAG = LoginPresenter::class.java.simpleName
    }
}