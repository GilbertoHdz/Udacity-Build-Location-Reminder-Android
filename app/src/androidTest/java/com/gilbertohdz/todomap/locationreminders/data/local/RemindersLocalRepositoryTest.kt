package com.gilbertohdz.todomap.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.gilbertohdz.todomap.locationreminders.data.dto.ReminderDTO
import com.gilbertohdz.todomap.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    DONE: Add testing implementation to the RemindersLocalRepository.kt

    private lateinit var database: RemindersDatabase
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupRepository() {
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java
        )
                .allowMainThreadQueries()
                .build()

        remindersLocalRepository = RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
        )
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun saveReminderAndRetrieveReminder() = runBlocking {
        val reminderDTO = ReminderDTO(
                "Veracruz",
                "State of Mexíco",
                "Mexíco",
                18.451271192325304,
                -95.50490466376762,
                "001"
        )

        // Given
        remindersLocalRepository.saveReminder(reminderDTO)

        // When
        val retrievedReminder = remindersLocalRepository.getReminder(reminderDTO.id)

        // Then
        assertThat(retrievedReminder is Result.Success, `is`(true))
        retrievedReminder as Result.Success

        assertThat(retrievedReminder.data.title, `is`(reminderDTO.title))
        assertThat(retrievedReminder.data.description, `is`(reminderDTO.description))
        assertThat(retrievedReminder.data.location, `is`(reminderDTO.location))
        assertThat(retrievedReminder.data.latitude, `is`(reminderDTO.latitude))
        assertThat(retrievedReminder.data.longitude, `is`(reminderDTO.longitude))
        assertThat(retrievedReminder.data.id, `is`(reminderDTO.id))
    }

    @Test
    fun deleteAllReminders() = runBlocking {
        // Given
        remindersLocalRepository.deleteAllReminders()

        // When
        val retrievedReminders = remindersLocalRepository.getReminders()
        retrievedReminders as Result.Success

        // Then
        assertThat(retrievedReminders.data.firstOrNull(), `is`(nullValue()))
    }
}