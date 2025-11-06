package com.example.rmaapp.fragments

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.rmaapp.EventNotificationReceiver
import com.example.rmaapp.R
import com.example.rmaapp.database.AppDatabase
import com.example.rmaapp.database.entities.Event
import com.example.rmaapp.widget.EventWidgetProvider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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

        createNotificationChannel()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        startDateButton.setOnClickListener { pickDate() }
        startTimeButton.setOnClickListener { pickTime(isStart = true) }
        endTimeButton.setOnClickListener { pickTime(isStart = false) }

        allDaySwitch.setOnCheckedChangeListener { _, isChecked ->
            startTimeButton.isEnabled = !isChecked
            endTimeButton.isEnabled = !isChecked

            if (isChecked) {
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
            true
        ).show()
    }

    private fun saveEvent() {
        if (!checkNotificationPermissions(requireContext())) {
            return
        }

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
            endDateTime = startDate!!.plusDays(1).atStartOfDay().minusSeconds(1)
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

            // Update the widget
            val intent = Intent(requireContext(), EventWidgetProvider::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(requireContext()).getAppWidgetIds(ComponentName(requireContext(), EventWidgetProvider::class.java))
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            requireContext().sendBroadcast(intent)

            requireActivity().supportFragmentManager.setFragmentResult("event_changed_key", Bundle())

            if (!resources.getBoolean(R.bool.isTablet)) {
                parentFragmentManager.popBackStack()
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleNotification(eventId: Int, eventTitle: String, eventStartTime: LocalDateTime) {
        val intent = Intent(requireContext(), EventNotificationReceiver::class.java).apply {
            putExtra(EventNotificationReceiver.Companion.EVENT_ID_EXTRA, eventId)
            putExtra(EventNotificationReceiver.Companion.EVENT_TITLE_EXTRA, eventTitle)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            eventId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAtMillis = eventStartTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            Log.e("AddEventFragment", "Failed to schedule exact alarm", e)
            Toast.makeText(requireContext(), "Could not schedule notification due to system restrictions.", Toast.LENGTH_LONG).show()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = EventNotificationReceiver.Companion.NOTIFICATION_CHANNEL_NAME
            val descriptionText = "Notifications for upcoming events"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(EventNotificationReceiver.Companion.NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkNotificationPermissions(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val isEnabled = notificationManager.areNotificationsEnabled()

            if (!isEnabled) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                context.startActivity(intent)
                return false
            }
        } else {
            val areEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()

            if (!areEnabled) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                context.startActivity(intent)
                return false
            }
        }
        return true
    }
}