package com.gilbertohdz.todomap.locationreminders

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.firebase.ui.auth.AuthUI
import com.gilbertohdz.todomap.R
import com.gilbertohdz.todomap.authentication.AuthenticationActivity
import com.gilbertohdz.todomap.utils.LocationUtils
import com.gilbertohdz.todomap.utils.LocationUtils.REQUEST_LOCATION_PERMISSION
import com.gilbertohdz.todomap.utils.LocationUtils.hasPermission
import kotlinx.android.synthetic.main.activity_reminders.*

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)
        enableDeviceLocation()
    }

    private fun enableDeviceLocation() {
        if (!LocationUtils.isPermissionGranted(this)) {
            LocationUtils.requestLocationPermission(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
            }
        }
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
