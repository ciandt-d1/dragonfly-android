package com.ciandt.dragonfly.example.features.login

import com.ciandt.dragonfly.example.shared.BasePresenterContract
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.firebase.auth.FirebaseUser

/**
 * Created by iluz on 6/20/17.
 */
interface LoginContract {

    interface View {

        fun startSignInWithGoogle()

        fun goToMain()

        fun showLoading()

        fun showError()
    }

    interface Presenter : BasePresenterContract<View> {

        fun checkSignInWithGoogle(result: GoogleSignInResult)

        fun signInWithGoogleFailed(errorCode: Int, errorMessage: String?)

        fun signInWithGoogle()
    }

    interface Interactor {

        fun signInWithGoogle(token: String, onSuccess: (FirebaseUser) -> Unit, onFailure: (Exception?) -> Unit)
    }

}