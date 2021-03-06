package com.ciandt.dragonfly.example.features.login

import com.ciandt.dragonfly.example.shared.BasePresenterContract
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.firebase.auth.FirebaseUser

interface LoginContract {

    interface View {

        fun startSignInWithGoogle()

        fun goToMain()

        fun cancel()

        fun showLoading()

        fun showError(exception: Exception)
    }

    interface Presenter : BasePresenterContract<View> {

        fun signInWithGoogle()

        fun checkSignInWithGoogle(result: GoogleSignInResult)

        fun signInWithGoogleFailed(errorCode: Int, errorMessage: String?)

        fun signInWithGoogleCanceled(networkAvailable: Boolean)
    }

    interface Interactor {

        fun signInWithGoogle(token: String, onSuccess: (FirebaseUser) -> Unit, onFailure: (Exception?) -> Unit)
    }

}