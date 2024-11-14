package com.example.warehouseapp.admin

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.warehouseapp.R
import com.example.warehouseapp.adapter.SalesDataAdapter
import com.example.warehouseapp.api.ApiService
import com.example.warehouseapp.api.RetrofitClient
import com.example.warehouseapp.auth.LoginActivity
import com.example.warehouseapp.databinding.FragmentAdminHomeBinding
import com.example.warehouseapp.model.FinancialReport
import com.example.warehouseapp.model.SalesDataCategory
import com.example.warehouseapp.util.readBaseUrl
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class AdminHomeFragment : Fragment() {

    private lateinit var binding: FragmentAdminHomeBinding
    private lateinit var pieChart: PieChart
    private lateinit var rvSalesData: RecyclerView
    private lateinit var salesDataAdapter: SalesDataAdapter
    private lateinit var spinnerFilterMonth: Spinner
    private lateinit var spinnerFilterYear: Spinner
    private lateinit var spinnerFilterType: Spinner

    private var selectedFilterMonth: String? = null
    private var selectedFilterDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdminHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        // Set up views
        pieChart = binding.pieChart
        rvSalesData = binding.rvSalesData
        rvSalesData.layoutManager = LinearLayoutManager(requireContext())
        spinnerFilterMonth = binding.spinnerFilterMonth
        spinnerFilterYear = binding.spinnerFilterYear
        spinnerFilterType = binding.spinnerFilterType

        // Initialize adapter with an empty list and set it on RecyclerView
        salesDataAdapter = SalesDataAdapter(emptyList())
        rvSalesData.adapter = salesDataAdapter

        // Set up Spinner for selecting filter type (This Month, Daily, Monthly)
        val filterTypes = arrayOf("This Month", "Daily", "Monthly")
        val filterTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filterTypes)
        filterTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilterType.adapter = filterTypeAdapter

        // Handle filter type selection changes
        spinnerFilterType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> { // This Month selected
                        spinnerFilterMonth.visibility = View.GONE
                        spinnerFilterYear.visibility = View.GONE
                        binding.btnFilterDate.visibility = View.GONE
                        binding.tvSelectedDate.visibility = View.GONE
                    }
                    1 -> { // Daily selected
                        spinnerFilterMonth.visibility = View.GONE
                        spinnerFilterYear.visibility = View.GONE
                        binding.btnFilterDate.visibility = View.VISIBLE
                        binding.tvSelectedDate.visibility = View.VISIBLE
                    }
                    2 -> { // Monthly selected
                        spinnerFilterMonth.visibility = View.VISIBLE
                        spinnerFilterYear.visibility = View.VISIBLE
                        binding.btnFilterDate.visibility = View.GONE
                        binding.tvSelectedDate.visibility = View.GONE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Default to monthly view or no action
            }
        }

        // Set up Spinner for selecting month
        val months = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilterMonth.adapter = monthAdapter

        // Set up Spinner for selecting year
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 5..currentYear + 5).toList().map { it.toString() }
        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilterYear.adapter = yearAdapter

        // Set up DatePicker for selecting exact date
        binding.btnFilterDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    selectedFilterDate = "$selectedYear-${String.format("%02d", selectedMonth + 1)}-${String.format("%02d", selectedDay)}"
                    binding.tvSelectedDate.text = selectedFilterDate
                }, year, month, day
            )
            datePicker.show()
        }

        // Handle filter button click
        binding.adminBtnFilter.setOnClickListener {
            val selectedMonthIndex = spinnerFilterMonth.selectedItemPosition + 1
            val selectedYear = binding.spinnerFilterYear.selectedItem.toString()
            selectedFilterMonth = "$selectedYear-${String.format("%02d", selectedMonthIndex)}"

            when (spinnerFilterType.selectedItemPosition) {
                0 -> fetchFinancialReportData(null, null) // This Month selected, fetch current month data
                1 -> fetchFinancialReportData(selectedFilterDate, null) // Daily selected, fetch by date
                2 -> fetchFinancialReportData(null, selectedFilterMonth) // Monthly selected, fetch by month
            }
        }

        // Handle clear filter button click
        binding.adminBtnClearFilter.setOnClickListener {
            spinnerFilterType.setSelection(0)
            spinnerFilterMonth.setSelection(0)
            spinnerFilterYear.setSelection(0)
            binding.tvSelectedDate.text = "No date selected"
            selectedFilterDate = null
            selectedFilterMonth = null
            fetchFinancialReportData(null, null) // Clear filters and fetch default data
        }

        // Handle logout button click
        binding.adminBtnLogout.setOnClickListener {
            clearTokenFromPreferences()
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        // Fetch current month data when fragment loads
        fetchFinancialReportData(null, null)

        return view
    }

    private fun clearTokenFromPreferences() {
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.remove("jwt_token")
        editor.apply()
    }

    private fun fetchFinancialReportData(filterDate: String?, filterMonth: String?) {
        val baseUrl = readBaseUrl(requireContext())
        val apiService = RetrofitClient.getRetrofitInstance(baseUrl)
            .create(ApiService::class.java)

        apiService.getFinancialReport("Monthly", filterMonth, filterDate).enqueue(object : Callback<FinancialReport> {
            override fun onResponse(call: Call<FinancialReport>, response: Response<FinancialReport>) {
                if (response.isSuccessful) {
                    val financialReport = response.body()

                    if (financialReport != null) {
                        setupPieChart(financialReport.salesDataCategory ?: emptyList())
                        salesDataAdapter.updateData(financialReport.salesDataProduct ?: emptyList())
                    } else {
                        Toast.makeText(requireContext(), "Financial report is empty", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch data: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FinancialReport>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupPieChart(data: List<SalesDataCategory>) {
        val entries = data.map { PieEntry(it.totalSalesCategory.toFloat(), it.category) }
        val dataSet = PieDataSet(entries, "Sales by Category").apply {
            colors = ColorTemplate.COLORFUL_COLORS.toList()
        }

        val pieData = PieData(dataSet).apply {
            setValueTextSize(12f)
            setValueTextColor(android.graphics.Color.BLACK)
        }

        pieChart.data = pieData
        pieChart.invalidate()
    }
}