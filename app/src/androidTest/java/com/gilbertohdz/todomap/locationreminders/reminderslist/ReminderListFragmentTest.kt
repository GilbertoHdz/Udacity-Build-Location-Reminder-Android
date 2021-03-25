package com.gilbertohdz.todomap.locationreminders.reminderslist

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.gilbertohdz.todomap.R
import com.gilbertohdz.todomap.fake.FakeDataSource
import com.gilbertohdz.todomap.locationreminders.data.dto.ReminderDTO
import com.gilbertohdz.todomap.locationreminders.data.dto.Result
import com.gilbertohdz.todomap.util.DataBindingIdlingResource
import com.gilbertohdz.todomap.util.monitorFragment
import com.gilbertohdz.todomap.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.junit.After
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
//    DONE: test the displayed data on the UI.
//    DONE: add testing for the error messages.

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var reminderListViewModel: RemindersListViewModel
    private val dataBindingIdlingResource = DataBindingIdlingResource()

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

    @Before
    fun registerIdlingResource(){
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource(){
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
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

    @Test
    fun setupRecyclerView() = runBlockingTest {
        // Setup
        val list = listOf(
                ReminderDTO("title A", "description A", "location A", (-360..360).random().toDouble(),(-360..360).random().toDouble()),
                ReminderDTO("title B", "description B", "location B", (-360..360).random().toDouble(),(-360..360).random().toDouble()),
                ReminderDTO("title C", "description C", "location C", (-360..360).random().toDouble(),(-360..360).random().toDouble())
        )

        list.forEach {
            fakeDataSource.saveReminder(it)
        }

        val reminders = (fakeDataSource.getReminders() as? Result.Success)?.data

        // Given
        val fragmentScenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario)

        // When
        val firstItem = reminders!![0]

        // Then
        onView(
                Matchers.allOf(
                        withText(firstItem.location),
                        childAtPosition(
                                childAtPosition(
                                        ViewMatchers.withId(R.id.reminderCardView),
                                        0
                                ),
                                2
                        ),
                        isDisplayed()
                )
        )
                .check(matches(withText(firstItem.location)))
    }

    private fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}