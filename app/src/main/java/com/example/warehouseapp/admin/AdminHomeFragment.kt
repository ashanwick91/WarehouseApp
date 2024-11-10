package com.example.warehouseapp.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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


class AdminHomeFragment : Fragment() {

    private lateinit var binding: FragmentAdminHomeBinding
    private lateinit var pieChart: PieChart
    private lateinit var rvSalesData: RecyclerView
    private lateinit var salesDataAdapter: SalesDataAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAdminHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.adminBtnLogout.setOnClickListener {
            // Clear the JWT token from SharedPreferences
            clearTokenFromPreferences()

            // Redirect to login screen
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        // Set up views
        pieChart = binding.pieChart
        rvSalesData = binding.rvSalesData
        rvSalesData.layoutManager = LinearLayoutManager(requireContext())

        // Initialize adapter with an empty list and set it on RecyclerView
        salesDataAdapter = SalesDataAdapter(emptyList())
        rvSalesData.adapter = salesDataAdapter

        fetchFinancialReportData() // Fetch and display financial data

        return view
    }

    private fun clearTokenFromPreferences() {
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.remove("jwt_token")
        editor.apply()
    }

    private fun setupPieChart(data: List<SalesDataCategory>) {
        // Log the entries being added to the PieChart
        Log.d("setupPieChart", "Data entries: $data")

        // Create PieEntries for each category
        val entries = data.map { PieEntry(it.totalSalesCategory.toFloat(), it.category) }

        // Create DataSet for PieChart
        val dataSet = PieDataSet(entries, "Sales by Category").apply {
            colors = ColorTemplate.COLORFUL_COLORS.toList()
        }

        // Configure and set data for PieChart
        val pieData = PieData(dataSet).apply {
            setValueTextSize(12f)
            setValueTextColor(android.graphics.Color.BLACK)
        }

        pieChart.data = pieData
        pieChart.invalidate() // Refresh the chart
    }

    private fun fetchFinancialReportData() {
        // Initialize ApiService using RetrofitClient
        val baseUrl = readBaseUrl(requireContext())
        val apiService = RetrofitClient.getRetrofitInstance(baseUrl)
            .create(ApiService::class.java)

        apiService.getFinancialReport("Monthly").enqueue(object : Callback<FinancialReport> {
            override fun onResponse(call: Call<FinancialReport>, response: Response<FinancialReport>) {
                if (response.isSuccessful) {
                    val financialReport = response.body()

                    if (financialReport != null) {
                        // Log the data to verify its contents
                        Log.d("FinancialReport", "SalesDataCategory: ${financialReport.salesDataCategory}")

                        // Pass the full list to setupPieChart
                        setupPieChart(financialReport.salesDataCategory ?: emptyList())
                        salesDataAdapter.updateData(financialReport.salesDataProduct ?: emptyList())
                    } else {
                        Log.e("FinancialReport", "Financial report is null")
                    }
                } else {
                    Log.e("FinancialReport", "Request failed with code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<FinancialReport>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}