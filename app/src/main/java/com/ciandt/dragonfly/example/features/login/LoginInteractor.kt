package com.ciandt.dragonfly.example.features.login

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class LoginInteractor(val firebaseAuth: FirebaseAuth) : LoginContract.Interactor {

    override fun signInWithGoogle(token: String, onSuccess: (FirebaseUser) -> Unit, onFailure: (Exception?) -> Unit) {
        firebaseAuth
                .signInWithCredential(GoogleAuthProvider.getCredential(token, null))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        onSuccess(task.result.user)
                    } else {
                        onFailure(task.exception)
                    }
                }
    }
}