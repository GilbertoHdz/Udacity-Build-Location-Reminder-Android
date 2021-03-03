package com.gilbertohdz.todomap.locationreminders

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.firebase.ui.auth.AuthUI
import com.gilbertohdz.todomap.R
import com.gilbertohdz.todomap.authentication.AuthenticationActivity
import kotlinx.android.synthetic.main.activity_reminders.*

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                (nav_host_fragment as NavHostFragment).navController.popBackStack()
                return true
            }
            R.id.logout -> {
                AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener {
                        startActivity(
                            Intent(
                                this@RemindersActivity,
                                AuthenticationActivity::class.java
                            )
                        )
                        finish()
                    }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
