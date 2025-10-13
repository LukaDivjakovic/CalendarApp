package com.example.rmaapp

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.rmaapp.database.AppDatabase
import com.example.rmaapp.database.entities.Event
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.ZoneId

class AddEventFragment : Fragment() {

    private lateinit var eventTitleInput: TextInputEditText
    private lateinit var eventDescriptionInput: TextInputEditText
    private lateinit var startDateButton: Button
    private lateinit var startTimeButton: Button
    private lateinit var endTimeButton: Button
    private lateinit var allDaySwitch: SwitchMaterial
    private lateinit var saveEventButton: Button

    private var startDate: LocalDate? = null
    private var startTime: LocalTime? = null
    private var endTime: LocalTime? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        eventTitleInput = view.findViewById(R.id.event_title_input)
        eventDescriptionInput = view.findViewById(R.id.event_description_input)
        startDateButton = view.findViewById(R.id.start_date_button)
        startTimeButton = view.findViewById(R.id.start_time_button)
        endTimeButton = view.findViewById(R.id.end_time_button)
        allDaySwitch = view.findViewById(R.id.all_day_switch)
        saveEventButton = view.findViewById(R.id.save_event_button)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        startDateButton.setOnClickListener { pickDate() }
        startTimeButton.setOnClickListener { pickTime(isStart = true) }
        endTimeButton.setOnClickListener { pickTime(isStart = false) }

        // Add a listener to the all-day switch
        allDaySwitch.setOnCheckedChangeListener { _, isChecked ->
            // Disable time and end date pickers when "All-day" is on
            startTimeButton.isEnabled = !isChecked
            endTimeButton.isEnabled = !isChecked

            if (isChecked) {
                // Clear time values if the switch is on
                startTime = null
                endTime = null
                startTimeButton.text = getString(R.string.select_time)
                endTimeButton.text = getString(R.string.select_time)
            }
        }

        saveEventButton.setOnClickListener { saveEvent() }
    }

    private fun pickDate() {
        val today = LocalDate.now()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                startDate = selectedDate
                startDateButton.text = selectedDate.toString()
            },
            today.year,
            today.monthValue - 1,
            today.dayOfMonth
        ).show()
    }

    private fun pickTime(isStart: Boolean) {
        val now = LocalTime.now()
        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                val selectedTime = LocalTime.of(hour, minute)
                if (isStart) {
                    startTime = selectedTime
                    startTimeButton.text = selectedTime.toString()
                } else {
                    endTime = selectedTime
                    endTimeButton.text = selectedTime.toString()
                }
            },
            now.hour,
            now.minute,
            true // Use 24-hour format
        ).show()
    }

    private fun saveEvent() {
        val title = eventTitleInput.text.toString()
        if (title.isBlank()) {
            Toast.makeText(requireContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val isAllDay = allDaySwitch.isChecked
        val startDateTime: LocalDateTime
        val endDateTime: LocalDateTime

        if (isAllDay) {
            if (startDate == null) {
                Toast.makeText(requireContext(), "Please select a date for the all-day event", Toast.LENGTH_SHORT).show()
                return
            }
            startDateTime = startDate!!.atStartOfDay()
            endDateTime = startDate!!.plusDays(1).atStartOfDay().minusSeconds(1) // End of the day
        } else {
            if (startDate == null || startTime == null || endTime == null) {
                Toast.makeText(requireContext(), "Please select start and end dates and times", Toast.LENGTH_SHORT).show()
                return
            }
            startDateTime = LocalDateTime.of(startDate, startTime)
            endDateTime = LocalDateTime.of(startDate, endTime)

            if (endDateTime.isBefore(startDateTime)) {
                Toast.makeText(requireContext(), "End time cannot be before start time", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val newEvent = Event(
            title = title,
            description = eventDescriptionInput.text.toString(),
            startTime = startDateTime,
            endTime = endDateTime,
            isAllDay = isAllDay
        )

        lifecycleScope.launch {
            val eventDao = AppDatabase.getDatabase(requireContext()).eventDao()
            val eventId = eventDao.insert(newEvent).toInt()

            scheduleNotification(eventId, title, startDateTime)

            Toast.makeText(requireContext(), "Event saved!", Toast.LENGTH_SHORT).show()

            // Notify other fragments that an event has changed
            requireActivity().supportFragmentManager.setFragmentResult("event_changed_key", Bundle())

            if(!resources.getBoolean(R.bool.isTablet)){
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun scheduleNotification(eventId: Int, eventTitle: String, eventStartTime: LocalDateTime) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                showPermissionDialog()
                return
            }
        }

        val intent = Intent(requireContext(), EventNotificationReceiver::class.java).apply {
            putExtra(EventNotificationReceiver.EVENT_ID_EXTRA, eventId)
            putExtra(EventNotificationReceiver.EVENT_TITLE_EXTRA, eventTitle)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            eventId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = eventStartTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("To ensure notifications are delivered on time, please grant the Alarms & Reminders permission.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
