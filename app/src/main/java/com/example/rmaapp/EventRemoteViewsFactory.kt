package com.example.rmaapp

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.rmaapp.database.AppDatabase
import com.example.rmaapp.database.entities.Event
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class EventRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var events: List<Event> = emptyList()

    override fun onCreate() {
        // Not needed for this implementation
    }

    override fun onDataSetChanged() {
        runBlocking {
            val db = AppDatabase.getDatabase(context)
            val today = LocalDate.now()
            val startOfDay = today.atStartOfDay()
            val endOfDay = today.atTime(LocalTime.MAX)
            events = db.eventDao().getEventsForDate(startOfDay, endOfDay).filter {
                it.startTime.isAfter(LocalDateTime.now())
            }.sortedBy { it.startTime }
        }
    }

    override fun onDestroy() {
        // Not needed for this implementation
    }

    override fun getCount(): Int = events.size

    override fun getViewAt(position: Int): RemoteViews {
        val event = events[position]
        val views = RemoteViews(context.packageName, R.layout.widget_item_layout)

        views.setTextViewText(R.id.widget_event_title, event.title)
        views.setTextViewText(R.id.widget_event_time, event.startTime.format(DateTimeFormatter.ofPattern("HH:mm")))

        // Set up the fill-in intent to handle clicks on individual items
        val fillInIntent = Intent().apply {
            putExtra("event_id", event.id)
        }
        views.setOnClickFillInIntent(R.id.widget_event_title, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = events[position].id.toLong()

    override fun hasStableIds(): Boolean = true
}
