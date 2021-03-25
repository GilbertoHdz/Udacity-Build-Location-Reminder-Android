package com.gilbertohdz.todomap.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.gilbertohdz.todomap.R
import com.gilbertohdz.todomap.base.BaseFragment
import com.gilbertohdz.todomap.base.NavigationCommand
import com.gilbertohdz.todomap.databinding.FragmentSaveReminderBinding
import com.gilbertohdz.todomap.locationreminders.geofence.GeofenceHelper
import com.gilbertohdz.todomap.locationreminders.reminderslist.ReminderDataItem
import com.gilbertohdz.todomap.utils.LocationUtils.hasPermission
import com.gilbertohdz.todomap.utils.LocationUtils.requestPermissionWithRationale
import com.gilbertohdz.todomap.utils.setDisplayHomeAsUpEnabled
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import java.util.*

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    private lateinit var geofencingClient: GeofencingClient
    private val GEOFENCE_RADIUS = 500f
    private lateinit var reminderData: ReminderDataItem
    private lateinit var geofenceHelper: GeofenceHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        geofencingClient = LocationServices.getGeofencingClient(requireContext())
        geofenceHelper = GeofenceHelper(requireContext())

        binding.selectLocation.setOnClickListener {
            // Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                    NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value
            val geofenceId = UUID.randomUUID().toString()

//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
            if (latitude != null && longitude != null && !TextUtils.isEmpty(title)) {
                addGeofence(LatLng(latitude, longitude), GEOFENCE_RADIUS, geofenceId)
            }

            _viewModel.validateAndSaveReminder(ReminderDataItem(title,description,location, latitude,longitude))

            _viewModel.navigateToReminderList.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                if(it){
                    view.findNavController().navigate(R.id.action_saveReminderFragment_to_reminderListFragment)
                    _viewModel.navigateToReminderList()
                }
            })
        }

        requestBackgroundLocationPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        // make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    private fun requestBackgroundLocationPermission() {
        val permissionApproved = requireContext().hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) ?: return

        if (permissionApproved) {
            Toast.makeText(requireContext(), "BackgroundLocation Permission granted", Toast.LENGTH_SHORT).show()
        } else {
            requestPermissionWithRationale(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE,
                    backgroundRationalSnackbar)
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence(
            latLng: LatLng,
            radius: Float,
            geofenceId: String
    ) {
        val geofence: Geofence = geofenceHelper.getGeofence(
                geofenceId,
                latLng,
                radius,
                Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
        )
        val geofencingRequest: GeofencingRequest = geofenceHelper.getGeofencingRequest(geofence)
        val pendingIntent: PendingIntent? = geofenceHelper.getGeofencePendingIntent()

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(OnSuccessListener<Void?> {
                    // Toast.makeText(context,"geofence added",Toast.LENGTH_LONG).show()
                    Log.d(TAG, "Geofence Added")
                })
                .addOnFailureListener(OnFailureListener { e ->
                    val errorMessage: String = geofenceHelper.getErrorString(e)
                    Toast.makeText(context, R.string.geofence_background_permission, Toast.LENGTH_LONG).show()
                    Log.d(TAG, "fail in creating geofence: $errorMessage")
                })
    }

    private val backgroundRationalSnackbar by lazy {
        Snackbar.make(
                binding.constraintLayoutReminder,
                R.string.background_location_permission_rationale,
                Snackbar.LENGTH_LONG
        )
                .setAction(R.string.ok) {
                    requestPermissions(
                            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                            REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE
                    )
                }
    }

    companion object {
        private const val REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE = 34
        private const val REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE = 56

        private const val TAG = "SaveReminderFragment"
    }
}
