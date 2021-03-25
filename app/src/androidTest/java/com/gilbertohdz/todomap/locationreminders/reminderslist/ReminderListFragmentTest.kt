package com.gilbertohdz.todomap.locationreminders.reminderslist

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.gilbertohdz.todomap.R
import com.gilbertohdz.todomap.fake.FakeDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

//    DONE: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    DONE: add testing for the error messages.

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var reminderListViewModel: RemindersListViewModel

    @Before
    fun setup() {
        fakeDataSource = FakeDataSource()
        reminderListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)

        // stop
        stopKoin()

        val appModule = module {
            single {
                reminderListViewModel
            }
        }

        // Start
        startKoin {
            modules(listOf(appModule))
        }
    }

    @Test
    fun navigateToAddReminder() = runBlockingTest {
        // Given
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // When
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Then
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    @Test
    fun errorMessageShown() = runBlockingTest {
        // Given
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // Then
        onView(withText("No Data")).check(matches(isDisplayed()))
    }
}