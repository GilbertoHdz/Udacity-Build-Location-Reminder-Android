package com.gilbertohdz.todomap.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.gilbertohdz.todomap.locationreminders.data.dto.ReminderDTO

import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

//    DONE: Add testing implementation to the RemindersDao.kt

    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initialiseDatabase(){
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDatabase() = database.close()

    @Test
    fun insertReminderDTOAndGetById() = runBlockingTest {
        val reminderDTO = ReminderDTO(
                "Bangkok",
                "Capital of Thailand",
                "Bangkok",
                13.7563,
                100.5018,
                "101"
        )

        // Given
        database.reminderDao().saveReminder(reminderDTO)

        // When
        val savedReminderDTO = database.reminderDao().getReminderById(reminderDTO.id)

        // Then
        assertThat<ReminderDTO>(savedReminderDTO, notNullValue())

        assertThat(savedReminderDTO?.id, `is`(reminderDTO.id))
        assertThat(savedReminderDTO?.title, `is`(reminderDTO.title))
        assertThat(savedReminderDTO?.description, `is`(reminderDTO.description))
        assertThat(savedReminderDTO?.location, `is`(reminderDTO.location))
        assertThat(savedReminderDTO?.latitude, `is`(reminderDTO.latitude))
        assertThat(savedReminderDTO?.longitude, `is`(reminderDTO.longitude))
        assertThat(savedReminderDTO?.id, `is`(reminderDTO.id))
    }

    @Test
    fun insertAndCleanDatabase() = runBlockingTest {
        val reminderDTO = ReminderDTO(
                "Veracruz",
                "State of Mexíco",
                "Mexíco",
                18.451271192325304,
                -95.50490466376762,
                "001"
        )

        // Given
        database.reminderDao().saveReminder(reminderDTO)
        database.reminderDao().deleteAllReminders()

        // When
        val savedRemindersDTO = database.reminderDao().getReminders()

        // Then
        assertThat(savedRemindersDTO.isEmpty(), `is`(true))
    }

}