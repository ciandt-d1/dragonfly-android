package com.ciandt.dragonfly.example.shared

import com.ciandt.dragonfly.infrastructure.DragonflyLogger
import com.google.firebase.auth.FirebaseAuth

/**
 * Created by iluz on 6/20/17.
 */
class LoginPresenter(val firebaseAuth: FirebaseAuth) : BasePresenter<LoginContract.View>(), LoginContract.Presenter {

    override fun signInAnonymously() {
        if (firebaseAuth.currentUser != null) {
            return
        }

        firebaseAuth.signInAnonymously()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && (firebaseAuth.currentUser != null)) {
                        DragonflyLogger.debug(LOG_TAG, "signInAnonymously: success")

                        firebaseAuth.currentUser?.let {
                            view?.onLoginSuccess(it)
                        }
                    } else {
                        DragonflyLogger.warn(LOG_TAG, "signInAnonymously: failure ${task.exception}")

                        view?.onLoginFailure()
                    }
                }
    }

    companion object {
        private val LOG_TAG = LoginPresenter::class.java.simpleName
    }
}