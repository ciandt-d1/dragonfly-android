package com.ciandt.dragonfly.example.features.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.features.modelselection.ModelSelectionActivity
import com.ciandt.dragonfly.example.infrastructure.extensions.makeGone
import com.ciandt.dragonfly.example.infrastructure.extensions.makeVisible
import com.ciandt.dragonfly.example.shared.BaseActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity(), LoginContract.View, GoogleApiClient.OnConnectionFailedListener {

    private val RESULT_CODE_SIGN_IN = 9001

    private var googleApiClient: GoogleApiClient? = null

    private lateinit var presenter: LoginContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        withGoogle.setOnClickListener {
            presenter.signInWithGoogle()
        }
    }

    override fun onResume() {
        super.onResume()

        presenter = LoginPresenter(LoginInteractor(FirebaseAuth.getInstance()))
        presenter.attachView(this)
    }

    override fun onPause() {
        super.onPause()

        presenter.detachView()
    }

    override fun startSignInWithGoogle() {
        if (googleApiClient == null) {

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

            googleApiClient = GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* Activity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build()
        }

        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signInIntent, RESULT_CODE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        presenter.attachView(this)

        if (requestCode == RESULT_CODE_SIGN_IN && resultCode == Activity.RESULT_OK) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            presenter.checkSignInWithGoogle(result)
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        presenter.signInWithGoogleFailed(connectionResult.errorCode, connectionResult.errorMessage)
    }

    override fun requiresUserToBeSignedIn(): Boolean {
        return false
    }

    override fun goToMain() {
        startActivity(ModelSelectionActivity.create(this))
        finish()
    }

    override fun showLoading() {
        progress.makeVisible()
    }

    override fun showError() {
        progress.makeGone()
        Snackbar.make(getRootView(), getString(R.string.login_error), Snackbar.LENGTH_LONG).show()
    }

    companion object {
        fun create(context: Context): Intent {
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            return intent
        }
    }

}
