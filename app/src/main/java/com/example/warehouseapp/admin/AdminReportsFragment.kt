package com.example.warehouseapp.admin

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.warehouseapp.adapter.SalesDataAdapter
import com.example.warehouseapp.api.ApiService
import com.example.warehouseapp.api.RetrofitClient
import com.example.warehouseapp.databinding.FragmentAdminReportsBinding
import com.example.warehouseapp.model.FinancialReport
import com.example.warehouseapp.model.SalesDataCategory
import com.example.warehouseapp.util.readBaseUrl
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileWriter
import java.io.OutputStreamWriter
import java.util.Calendar

class AdminReportsFragment : Fragment() {

    private lateinit var binding: FragmentAdminReportsBinding
    private lateinit var pieChart: PieChart
    private lateinit var rvSalesData: RecyclerView
    private lateinit var salesDataAdapter: SalesDataAdapter
    private lateinit var spinnerFilterMonth: Spinner
    private lateinit var spinnerFilterYear: Spinner
    private lateinit var spinnerFilterType: Spinner
    private lateinit var apiService: ApiService

    private var selectedFilterMonth: String? = null
    private var selectedFilterDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdminReportsBinding.inflate(inflater, container, false)
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
        val baseUrl = readBaseUrl(requireContext())
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        // Set up Spinner for selecting filter type (This Month, Daily, Monthly)
        val filterTypes = arrayOf("This Month", "Daily", "Monthly")
        val filterTypeAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filterTypes)
        filterTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilterType.adapter = filterTypeAdapter

        // Handle filter type selection changes
        spinnerFilterType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
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
        val monthAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilterMonth.adapter = monthAdapter

        // Set up Spinner for selecting year
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 5..currentYear + 5).toList().map { it.toString() }
        val yearAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilterYear.adapter = yearAdapter

        // Set up DatePicker for selecting exact date
        /*binding.btnFilterDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    selectedFilterDate = "$selectedYear-${
                        String.format(
                            "%02d",
                            selectedMonth + 1
                        )
                    }-${String.format("%02d", selectedDay)}"
                    binding.tvSelectedDate.text = selectedFilterDate
                }, year, month, day
            )
            datePicker.show()
        }*/

        binding.btnFilterDate.setOnClickListener {
            // Build constraints for the Date Picker (optional)
            val constraintsBuilder = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now()) // Optional: Restrict to only future dates

            // Build the Material Date Picker
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraintsBuilder.build())
                .build()

            // Show the Date Picker
            datePicker.show(parentFragmentManager, "MATERIAL_DATE_PICKER")

            // Listener to handle the selected date
            datePicker.addOnPositiveButtonClickListener { selection ->
                if (selection != null) {
                    // Convert the selection (in milliseconds) to a readable date using Calendar
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = selection

                    // Extract the year, month, and day
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH) + 1 // Months are 0-indexed
                    val day = calendar.get(Calendar.DAY_OF_MONTH)

                    // Format the date as "yyyy-MM-dd"
                    val selectedDate =
                        "$year-${String.format("%02d", month)}-${String.format("%02d", day)}"

                    // Set the selected date in the TextView and variable
                    selectedFilterDate = selectedDate
                    binding.tvSelectedDate.text = selectedDate
                }
            }
        }

        // Handle filter button click
        binding.adminBtnFilter.setOnClickListener {
            val selectedMonthIndex = spinnerFilterMonth.selectedItemPosition + 1
            val selectedYear = binding.spinnerFilterYear.selectedItem.toString()
            selectedFilterMonth = "$selectedYear-${String.format("%02d", selectedMonthIndex)}"

            when (spinnerFilterType.selectedItemPosition) {
                0 -> fetchFinancialReportData(
                    null,
                    null
                ) // This Month selected, fetch current month data
                1 -> fetchFinancialReportData(
                    selectedFilterDate,
                    null
                ) // Daily selected, fetch by date
                2 -> fetchFinancialReportData(
                    null,
                    selectedFilterMonth
                ) // Monthly selected, fetch by month
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

        // Fetch current month data when fragment loads
        fetchFinancialReportData(null, null)

        // Handle download button click
        binding.adminBtnDownloadReport.setOnClickListener {
            val selectedMonthIndex = spinnerFilterMonth.selectedItemPosition + 1
            val selectedYear = binding.spinnerFilterYear.selectedItem.toString()
            selectedFilterMonth = "$selectedYear-${String.format("%02d", selectedMonthIndex)}"

            when (spinnerFilterType.selectedItemPosition) {
                0 -> fetchFinancialReportDataForCsv(null, null) // This Month
                1 -> fetchFinancialReportDataForCsv(selectedFilterDate, null) // Daily
                2 -> fetchFinancialReportDataForCsv(null, selectedFilterMonth) // Monthly
            }
        }


        return view
    }

    private fun fetchFinancialReportData(filterDate: String?, filterMonth: String?) {
        val baseUrl = readBaseUrl(requireContext())
        val apiService = RetrofitClient.getRetrofitInstance(baseUrl)
            .create(ApiService::class.java)

        apiService.getFinancialReport("Monthly", filterMonth, filterDate)
            .enqueue(object : Callback<FinancialReport> {
                override fun onResponse(
                    call: Call<FinancialReport>,
                    response: Response<FinancialReport>
                ) {
                    if (response.isSuccessful) {
                        val financialReport = response.body()

                        if (financialReport != null) {
                            setupPieChart(financialReport.salesDataCategory ?: emptyList())
                            salesDataAdapter.updateData(
                                financialReport.salesDataProduct ?: emptyList()
                            )
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Financial report is empty",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to fetch data: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<FinancialReport>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
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

    private fun fetchFinancialReportDataForCsv(filterDate: String?, filterMonth: String?) {
        val baseUrl = readBaseUrl(requireContext())
        val apiService = RetrofitClient.getRetrofitInstance(baseUrl)
            .create(ApiService::class.java)

        apiService.getFinancialReport("Monthly", filterMonth, filterDate)
            .enqueue(object : Callback<FinancialReport> {
                override fun onResponse(
                    call: Call<FinancialReport>,
                    response: Response<FinancialReport>
                ) {
                    if (response.isSuccessful) {
                        val financialReport = response.body()
                        financialReport?.let {
                            generateCsvFile(it)
                            saveCsvToDownloads(it) // Save to Downloads
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to fetch data", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<FinancialReport>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }


    private fun generateCsvFile(financialReport: FinancialReport) {
        try {
            // Get external storage directory
            val downloadsDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val csvFile = File(downloadsDir, "Financial_Report.csv")

            // Write data to CSV
            FileWriter(csvFile).use { writer ->
                // Write headers
                writer.append("Category,Total Sales,Product,Profit\n")

                // Write category data
                financialReport.salesDataCategory?.forEach {
                    writer.append("${it.category},${it.totalSalesCategory},,\n")
                }

                // Write product data
                financialReport.salesDataProduct?.forEach {
                    writer.append(",,${it.product},${it.product}\n")
                }
            }

            // Notify user of successful file creation
            Toast.makeText(
                requireContext(),
                "CSV file created at: ${csvFile.absolutePath}",
                Toast.LENGTH_SHORT
            ).show()

            // Share or download the file
            shareCsvFile(csvFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                requireContext(),
                "Error creating CSV file: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun shareCsvFile(csvFile: File) {
        val fileUri: Uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            csvFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(intent, "Share CSV"))
    }

    private fun saveCsvToDownloads(financialReport: FinancialReport) {
        try {
            val contentResolver = requireContext().contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "Financial_Report.csv")
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri =
                contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    val writer = OutputStreamWriter(outputStream)
                    writer.append("Category,Total Sales,Product,Profit\n")
                    financialReport.salesDataCategory?.forEach { category ->
                        writer.append("${category.category},${category.totalSalesCategory},,\n")
                    }
                    financialReport.salesDataProduct?.forEach { product ->
                        writer.append(",,${product.product},${product.product}\n")
                    }
                    writer.close()
                }
                Toast.makeText(requireContext(), "CSV saved to Downloads", Toast.LENGTH_SHORT)
                    .show()
            } ?: run {
                Toast.makeText(requireContext(), "Error saving CSV", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

}