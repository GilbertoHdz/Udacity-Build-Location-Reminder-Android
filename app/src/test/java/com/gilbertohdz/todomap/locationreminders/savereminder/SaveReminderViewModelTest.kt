package com.gilbertohdz.todomap.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.gilbertohdz.todomap.R
import com.gilbertohdz.todomap.base.NavigationCommand
import com.gilbertohdz.todomap.locationreminders.data.FakeDataSource
import com.gilbertohdz.todomap.locationreminders.reminderslist.ReminderDataItem
import com.gilbertohdz.todomap.locationreminders.utils.MainCoroutineRule
import com.gilbertohdz.todomap.locationreminders.utils.getOrAwaitValue

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class SaveReminderViewModelTest {

    // DONE: provide testing to the SaveReminderView and its live data objects

    @get: Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @After
    fun tearDown() {
        stopKoin()
    }

    @Before
    fun createViewModel(){
        stopKoin()
        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @Test
    fun checkLoading(){
        val reminderDataItem = ReminderDataItem(
                "Veracruz",
                "State of Mexíco",
                "Mexíco",
                18.451271192325304,
                -95.50490466376762,
                "001"
        )

        // Given
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.saveReminder(reminderDataItem)

        // When
        var showLoading = saveReminderViewModel.showLoading.getOrAwaitValue()

        // Then
        assertThat(showLoading, `is`(true))
        mainCoroutineRule.resumeDispatcher()
        showLoading = saveReminderViewModel.showLoading.getOrAwaitValue()
        assertThat(showLoading, `is`(false))
    }

    @Test
    fun checkToast(){
        val reminderDataItem = ReminderDataItem(
                "Veracruz",
                "State of Mexíco",
                "Mexíco",
                18.451271192325304,
                -95.50490466376762,
                "001"
        )
        // Given
        saveReminderViewModel.saveReminder(reminderDataItem)

        // When
        val showToast = saveReminderViewModel.showToast.getOrAwaitValue()

        // Then
        assertThat(showToast, `is`("Reminder Saved !"))
    }

    @Test
    fun checkNavigation(){
        val reminderDataItem = ReminderDataItem(
                "Veracruz",
                "State of Mexíco",
                "Mexíco",
                18.451271192325304,
                -95.50490466376762,
                "001"
        )
        // Given
        saveReminderViewModel.saveReminder(reminderDataItem)

        // When
        val navigate = saveReminderViewModel.navigationCommand.getOrAwaitValue()
        navigate as NavigationCommand

        // Then
        assertThat(navigate, instanceOf(NavigationCommand.Back::class.java))
    }

    @Test
    fun shouldReturnErrorNoTitle() {
        val reminderDataItem = ReminderDataItem(
                null,
                "State of Mexíco",
                "Mexíco",
                18.451271192325304,
                -95.50490466376762,
                "001"
        )
        assertThat(saveReminderViewModel.validateEnteredData(reminderDataItem), `is`(false))
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }

    @Test
    fun shouldReturnErrorNoLocation() {
        val reminderDataItem = ReminderDataItem(
                "Veracruz",
                "State of Mexíco",
                null,
                18.451271192325304,
                -95.50490466376762,
                "001"
        )
        assertThat(saveReminderViewModel.validateEnteredData(reminderDataItem), `is`(false))
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
    }
}