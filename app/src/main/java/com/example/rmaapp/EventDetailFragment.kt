package com.example.rmaapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.rmaapp.database.AppDatabase
import com.example.rmaapp.database.entities.Event
import kotlinx.coroutines.launch

class EventDetailFragment : Fragment() {

    private lateinit var event: Event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            event = it.getParcelable("event")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleTextView = view.findViewById<TextView>(R.id.detail_event_title)
        val timeTextView = view.findViewById<TextView>(R.id.detail_event_time)
        val descriptionTextView = view.findViewById<TextView>(R.id.detail_event_description)
        val exitButton = view.findViewById<ImageButton>(R.id.exit_button)
        val deleteButton = view.findViewById<Button>(R.id.delete_event_button)

        titleTextView.text = event.title
        descriptionTextView.text = event.description

        if (event.isAllDay) {
            timeTextView.text = "All Day"
        } else {
            val startTime = event.startTime.toLocalTime().toString()
            val endTime = event.endTime.toLocalTime().toString()
            timeTextView.text = "$startTime - $endTime"
        }

        exitButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        deleteButton.setOnClickListener {
            lifecycleScope.launch {
                val eventDao = AppDatabase.getDatabase(requireContext()).eventDao()
                eventDao.delete(event)
                
                // Notify other fragments that an event has changed
                requireActivity().supportFragmentManager.setFragmentResult("event_changed_key", Bundle())
                
                parentFragmentManager.popBackStack()
            }
        }
    }

    companion object {
        fun newInstance(event: Event) = EventDetailFragment().apply {
            arguments = Bundle().apply {
                putParcelable("event", event)
            }
        }
    }
}