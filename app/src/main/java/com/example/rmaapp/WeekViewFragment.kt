package com.example.rmaapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.time.DayOfWeek
import java.time.LocalDate

class WeekViewFragment : Fragment() {

    private var date: LocalDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            date = it.getSerializable("date") as LocalDate?
        }
        requireActivity().supportFragmentManager.setFragmentResultListener("event_changed_key", this) { _, _ ->
            childFragmentManager.fragments.forEach { fragment ->
                (fragment as? DayColumnFragment)?.refreshEvents()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_week_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val weekToShow = date ?: LocalDate.now()
        var currentMonday = weekToShow
        while (currentMonday.dayOfWeek != DayOfWeek.MONDAY) {
            currentMonday = currentMonday.minusDays(1)
        }

        val containerIds = listOf(
            R.id.day_1_container,
            R.id.day_2_container,
            R.id.day_3_container,
            R.id.day_4_container,
            R.id.day_5_container,
            R.id.day_6_container,
            R.id.day_7_container
        )

        for (i in 0..6) {
            val day = currentMonday.plusDays(i.toLong())
            val dayColumnFragment = DayColumnFragment.newInstance(day, showOutline = true)
            childFragmentManager.beginTransaction()
                .replace(containerIds[i], dayColumnFragment)
                .commit()
        }
    }

    companion object {
        fun newInstance(date: LocalDate): WeekViewFragment {
            val fragment = WeekViewFragment()
            val args = Bundle()
            args.putSerializable("date", date)
            fragment.arguments = args
            return fragment
        }
    }
}
