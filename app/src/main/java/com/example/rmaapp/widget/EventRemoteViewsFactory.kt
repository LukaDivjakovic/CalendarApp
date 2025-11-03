package com.example.rmaapp.widget

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.rmaapp.R
import com.example.rmaapp.database.AppDatabase
import com.example.rmaapp.database.entities.Event
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class EventRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var listItems: List<Any> = emptyList()

    private val VIEW_TYPE_HEADER = 0
    private val VIEW_TYPE_EVENT = 1

    override fun onCreate() {}

    override fun onDataSetChanged() {
        runBlocking {
            val db = AppDatabase.getDatabase(context)
            val today = LocalDate.now()
            val startOfDay = today.atStartOfDay()
            val endOfDay = today.atTime(LocalTime.MAX)

            val allEventsToday = db.eventDao().getEventsForDate(startOfDay, endOfDay)

            val allDayEvents = allEventsToday.filter { it.isAllDay }.sortedBy { it.title }
            val timedEvents = allEventsToday
                .filter { !it.isAllDay && it.startTime.isAfter(LocalDateTime.now()) }
                .sortedBy { it.startTime }

            val combinedList = mutableListOf<Any>()
            if (allDayEvents.isNotEmpty()) {
                combinedList.add("All-Day")
                combinedList.addAll(allDayEvents)
            }
            if (timedEvents.isNotEmpty()) {
                combinedList.add("Upcoming")
                combinedList.addAll(timedEvents)
            }
            listItems = combinedList
        }
    }

    override fun onDestroy() {}

    override fun getCount(): Int = listItems.size

    override fun getViewAt(position: Int): RemoteViews {
        val item = listItems[position]
        val viewType = getItemViewType(position)

        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val views = RemoteViews(context.packageName, R.layout.widget_header_layout)
                views.setTextViewText(R.id.widget_header, item as String)
                views
            }
            else -> { // VIEW_TYPE_EVENT
                val event = item as Event
                val views = RemoteViews(context.packageName, R.layout.widget_item_layout)

                views.setTextViewText(R.id.widget_event_title, event.title)

                if (event.isAllDay) {
                    views.setViewVisibility(R.id.widget_event_time, View.GONE)
                } else {
                    views.setViewVisibility(R.id.widget_event_time, View.VISIBLE)
                    views.setTextViewText(R.id.widget_event_time, event.startTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                }

                val fillInIntent = Intent().apply {
                    putExtra("event_id", event.id)
                }
                views.setOnClickFillInIntent(R.id.widget_event_title, fillInIntent)
                views
            }
        }
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 2

    // This is a helper function, not an override
    private fun getItemViewType(position: Int): Int {
        return if (listItems[position] is String) VIEW_TYPE_HEADER else VIEW_TYPE_EVENT
    }

    override fun getItemId(position: Int): Long {
        val item = listItems[position]
        return if (item is Event) item.id.toLong() else position.toLong()
    }

    override fun hasStableIds(): Boolean = true
}
