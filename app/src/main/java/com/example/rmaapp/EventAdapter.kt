package com.example.rmaapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rmaapp.database.entities.Event

class EventAdapter(
    private val events: List<Event>,
    private val onItemClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event_card, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.bind(event)
        holder.itemView.setOnClickListener { onItemClick(event) }
    }

    override fun getItemCount(): Int = events.size

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.event_title)
        private val timeTextView: TextView = itemView.findViewById(R.id.event_time)

        @SuppressLint("SetTextI18n")
        fun bind(event: Event) {
            titleTextView.text = event.title
            if (event.isAllDay) {
                timeTextView.text = "All Day"
            } else {
                val startTime = event.startTime.toLocalTime().toString()
                val endTime = event.endTime.toLocalTime().toString()
                timeTextView.text = "$startTime - $endTime"
            }
        }
    }
}