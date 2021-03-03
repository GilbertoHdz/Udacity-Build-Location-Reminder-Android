package com.gilbertohdz.todomap.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.gilbertohdz.todomap.R
import com.gilbertohdz.todomap.locationreminders.RemindersActivity
import com.gilbertohdz.todomap.utils.Prefs
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_authentication.*

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            // already signed in
            navigateToRemindersScreen()
        } else {
            authLogin.setOnClickListener {
                authSetup()
            }
        }

        // TODO: a bonus is to customize the sign in flow to look nice using :
        // https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == RESULT_OK) {
                Prefs(this).userToken = response!!.idpToken
                navigateToRemindersScreen()
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    showMessage(R.string.sign_in_cancelled);
                    return;
                }

                if (response.error?.errorCode == ErrorCodes.NO_NETWORK) {
                    showMessage(R.string.no_internet_connection);
                    return;
                }

                showMessage(R.string.no_internet_connection);
                Log.e(TAG, "Sign-in error: ", response.getError());
            }
        }
    }

    private fun showMessage(resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToRemindersScreen() {
        startActivity(Intent(this, RemindersActivity::class.java))
        finish()
    }

    private fun authSetup() {
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(
                    listOf(
                        AuthUI.IdpConfig.EmailBuilder().build(),
                        AuthUI.IdpConfig.GoogleBuilder().build()
                    )
                )
                .build(),
            RC_SIGN_IN
        )
    }

    companion object {
        private const val TAG = "AuthenticationActivity"
        private const val RC_SIGN_IN = 123
    }
}
