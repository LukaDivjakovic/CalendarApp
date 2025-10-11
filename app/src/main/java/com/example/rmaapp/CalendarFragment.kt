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

        val addEventButton = view.findViewById<ImageView>(R.id.baseline_add_box_24)
        val weekViewButton = view.findViewById<ImageView>(R.id.baseline_calendar_view_week_24)
        val dayViewButton = view.findViewById<ImageView>(R.id.baseline_calendar_view_day_24)

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