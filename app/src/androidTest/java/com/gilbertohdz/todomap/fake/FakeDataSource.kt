package com.gilbertohdz.todomap.fake

import com.gilbertohdz.todomap.locationreminders.data.ReminderDataSource
import com.gilbertohdz.todomap.locationreminders.data.dto.ReminderDTO
import com.gilbertohdz.todomap.locationreminders.data.dto.Result

class FakeDataSource(var tasks: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

    // DONE: Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        tasks?.let { return Result.Success(it) }
        return Result.Error(FAKE_NO_REMINDERS_FOUND)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        tasks?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        tasks?.firstOrNull { it.id == id }?.let { return Result.Success(it) }
        return Result.Error(FAKE_REMINDER_NOT_FOUND)
    }

    override suspend fun deleteAllReminders() {
        tasks = mutableListOf()
    }

    companion object {
        const val FAKE_REMINDER_NOT_FOUND = "Reminder not found"
        const val FAKE_NO_REMINDERS_FOUND = "No reminders found"
    }
}