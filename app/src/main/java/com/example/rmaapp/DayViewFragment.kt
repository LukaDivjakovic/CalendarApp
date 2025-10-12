package com.example.rmaapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.time.LocalDate

class DayViewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_day_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            val today = LocalDate.now()
            val dayColumnFragment = DayColumnFragment.newInstance(today, showOutline = false)
            childFragmentManager.beginTransaction()
                .replace(R.id.day_column_container, dayColumnFragment)
                .commit()
        }
    }
}
