package com.ciandt.dragonfly.example.shared

import com.ciandt.dragonfly.infrastructure.DragonflyLogger
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth


/**
 * Created by iluz on 6/20/17.
 */
class LoginPresenter(firebaseAuth: FirebaseAuth) : BasePresenter<LoginContract.View>(), LoginContract.Presenter {
    val firebaseAuth = firebaseAuth

    override fun signInAnonymously() {
        if (firebaseAuth.currentUser != null) {
            return
        }

        firebaseAuth.signInAnonymously()
                .addOnCompleteListener(object : OnCompleteListener<AuthResult> {
                    override fun onComplete(task: Task<AuthResult>) {
                        if (task.isSuccessful() && (firebaseAuth.currentUser != null)) {
                            DragonflyLogger.debug(LOG_TAG, "signInAnonymously: success")

                            firebaseAuth.currentUser?.let {
                                view?.onLoginSuccess(it)
                            }
                        } else {
                            DragonflyLogger.warn(LOG_TAG, "signInAnonymously: failure ${task.getException()}")

                            view?.onLoginFailure()
                        }
                    }
                })
    }

    companion object {
        private val LOG_TAG = LoginPresenter::class.java.simpleName
    }
}