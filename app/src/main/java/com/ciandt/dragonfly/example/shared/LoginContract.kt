package com.ciandt.dragonfly.example.shared

import com.google.firebase.auth.FirebaseUser

/**
 * Created by iluz on 6/20/17.
 */
interface LoginContract {
    interface View {
        fun onLoginSuccess(user: FirebaseUser)

        fun onLoginFailure()
    }

    interface Presenter : BasePresenterContract<View> {

        fun signInAnonymously()
    }
}