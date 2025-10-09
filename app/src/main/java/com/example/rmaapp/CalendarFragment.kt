package com.example.rmaapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment

class CalendarFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        val addEventButton = view.findViewById<ImageView>(R.id.add_event_button)
        val weekViewButton = view.findViewById<ImageView>(R.id.week_view_button)
        val dayViewButton = view.findViewById<ImageView>(R.id.day_view_button)

        addEventButton.setOnClickListener {
            val addEventFragment = AddEventFragment()
            addEventFragment.show(parentFragmentManager, "AddEventFragment")
        }

        weekViewButton.setOnClickListener {
            Toast.makeText(requireContext(), "Week view clicked", Toast.LENGTH_SHORT).show()
        }

        dayViewButton.setOnClickListener {
            Toast.makeText(requireContext(), "Day view clicked", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}