package com.gilbertohdz.todomap.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import androidx.core.app.JobIntentService
import com.gilbertohdz.todomap.locationreminders.data.ReminderDataSource
import com.google.android.gms.location.Geofence
import com.gilbertohdz.todomap.locationreminders.data.dto.ReminderDTO
import com.gilbertohdz.todomap.locationreminders.data.dto.Result
import com.gilbertohdz.todomap.locationreminders.data.local.RemindersLocalRepository
import com.gilbertohdz.todomap.locationreminders.reminderslist.ReminderDataItem
import com.gilbertohdz.todomap.utils.sendNotification
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext
import org.koin.android.ext.android.get

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private val TAG = "GeofenceService"
    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob
    private val repository: ReminderDataSource by inject()

    companion object {
        private const val JOB_ID = 573

        // DONE: call this to start the JobIntentService to handle the geofencing transition events
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        //DONE: handle the geofencing transition events and
        // send a notification to the user when he enters the geofence area
        //DONE call @sendNotification
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        val geofenceList: List<Geofence> =
            geofencingEvent.triggeringGeofences
        sendNotification(geofenceList)
    }

    // DONE: get the request id of the current geofence
    private fun sendNotification(triggeringGeofences: List<Geofence>) {
        val requestId = when {
            triggeringGeofences.isNotEmpty() -> {
                Log.d(TAG, "sendNotification: " + triggeringGeofences[0].requestId)
                triggeringGeofences[0].requestId
            } else -> {
                Log.e(TAG, "No Geofence Trigger Found !")
                return
            }
        }

        if(TextUtils.isEmpty(requestId)) return

        //Get the local repository instance
        //val remindersLocalRepository: RemindersLocalRepository by inject()
//        Interaction to the repository has to be through a coroutine scope
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            //get the reminder with the request id
            val result = repository.getReminder(requestId)
            if (result is Result.Success<ReminderDTO>) {
                val reminderDTO = result.data
                //send a notification to the user with the reminder details
                sendNotification(
                    this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                        reminderDTO.title,
                        reminderDTO.description,
                        reminderDTO.location,
                        reminderDTO.latitude,
                        reminderDTO.longitude,
                        reminderDTO.id
                    )
                )
            }
        }
    }

}