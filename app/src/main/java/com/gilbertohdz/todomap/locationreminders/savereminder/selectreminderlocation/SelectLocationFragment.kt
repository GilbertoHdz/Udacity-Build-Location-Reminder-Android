package com.gilbertohdz.todomap.locationreminders.savereminder.selectreminderlocation


import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.gilbertohdz.todomap.R
import com.gilbertohdz.todomap.base.BaseFragment
import com.gilbertohdz.todomap.base.NavigationCommand
import com.gilbertohdz.todomap.databinding.FragmentSelectLocationBinding
import com.gilbertohdz.todomap.locationreminders.savereminder.SaveReminderViewModel
import com.gilbertohdz.todomap.utils.LocationUtils
import com.gilbertohdz.todomap.utils.setDisplayHomeAsUpEnabled
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import org.koin.android.ext.android.inject
import java.lang.Exception

class SelectLocationFragment : BaseFragment(),
    OnMapReadyCallback,
    GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener,
    GoogleMap.OnMapClickListener,
    GoogleMap.OnPoiClickListener,
    GoogleMap.OnMarkerClickListener {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var mPointOfInterest: PointOfInterest
    private lateinit var mMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        binding.selectLocationMapView.onCreate(savedInstanceState)
        binding.selectLocationMapView.getMapAsync(this)
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected

//        TODO: call this function after the user confirms on the selected location
        onLocationSelected()

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.selectLocationMapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.selectLocationMapView.onStop()
    }

    override fun onResume() {
        super.onResume()
        binding.selectLocationMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.selectLocationMapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.selectLocationMapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.selectLocationMapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.selectLocationMapView.onSaveInstanceState(outState)
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        if (::mPointOfInterest.isInitialized) {
            _viewModel.reminderSelectedLocationStr.value = mPointOfInterest.name
            _viewModel.selectedPOI.value = mPointOfInterest
            _viewModel.latitude.value = mPointOfInterest.latLng.latitude
            _viewModel.longitude.value = mPointOfInterest.latLng.longitude
        } else {
            _viewModel.showSnackBarInt.value = R.string.err_select_location
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            true
        }
        R.id.hybrid_map -> {
            true
        }
        R.id.satellite_map -> {
            true
        }
        R.id.terrain_map -> {
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        defaultCenterLocation(googleMap)

        if (!LocationUtils.isPermissionGranted(requireContext())) {
            setMapStyle(googleMap)
            googleMap.isMyLocationEnabled = true
            googleMap.setOnMyLocationButtonClickListener(this)
            googleMap.setOnMyLocationClickListener(this)
            googleMap.setOnMapClickListener(this)
            googleMap.setOnMarkerClickListener(this)
            googleMap.setOnPoiClickListener(this)

            saveSelectedLocation()
        } else {
            LocationUtils.requestLocationPermission(requireActivity())
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(requireContext(), "MyLocation button clicked", Toast.LENGTH_SHORT).show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(requireContext(), "Location-click:\n$location", Toast.LENGTH_LONG).show()
    }

    override fun onMapClick(latLong: LatLng?) {
        latLong?.let {
            Toast.makeText(requireContext(), "Map-click: ${latLong.latitude}, ${latLong.longitude}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPoiClick(poi: PointOfInterest) {
        val poiMarker = mMap.addMarker(
                MarkerOptions()
                        .position(poi.latLng)
                        .title(poi.name)
        )
        val zoomLevel = 15f
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(poi.latLng, zoomLevel))
        poiMarker.showInfoWindow()
        mPointOfInterest = poi
    }

    private fun saveSelectedLocation() {
        binding.selectLocationSaveAction.setOnClickListener {
            onLocationSelected()
            _viewModel.navigationCommand.postValue(NavigationCommand.Back)
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        marker?.let {
            Toast.makeText(requireContext(), "Marker-click: ${marker.title}", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    private fun setMapStyle(googleMap: GoogleMap) {
        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    private fun defaultCenterLocation(googleMap: GoogleMap) {
        val mexicoCenter = LatLng(19.88734529086868, -99.11100515334736)
        googleMap.apply {
            addMarker(
                MarkerOptions()
                    .position(mexicoCenter)
                    .title("México")
            )
            moveCamera(CameraUpdateFactory.newLatLng(mexicoCenter))
        }
    }

    companion object {
        private const val TAG = "SelectLocationFragment"
    }
}
