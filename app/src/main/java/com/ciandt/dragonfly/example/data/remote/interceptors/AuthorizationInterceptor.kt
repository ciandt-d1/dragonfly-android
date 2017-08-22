package com.ciandt.dragonfly.example.data.remote.interceptors

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

class AuthorizationInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (token == null) {
            refreshToken()
        }

        val response = signRequestAndProceed(chain)
        if (response.code() == 401 || response.code() == 403) {
            refreshToken()
            return signRequestAndProceed(chain)
        }

        return response
    }

    private fun refreshToken() {
        val user = FirebaseAuth.getInstance().currentUser ?: throw RuntimeException("No user signed in")
        token = Tasks.await(user.getIdToken(true), 10L, TimeUnit.SECONDS).token ?: throw RuntimeException("Token is null")
    }

    private fun signRequestAndProceed(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()

        return chain.proceed(request)
    }

    companion object {
        private var token: String? = null
    }
}