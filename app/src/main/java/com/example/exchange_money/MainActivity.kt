package com.example.exchange_money

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : AppCompatActivity() {

    // 1. Sử dụng tỷ giá cố định, thiết lập sẵn trong mã nguồn
    private val exchangeRates = mapOf(
        "USD" to BigDecimal("1.0"),          // Đô la Mỹ
        "VND" to BigDecimal("25450.0"),      // Đồng Việt Nam
        "EUR" to BigDecimal("0.92"),         // Euro
        "JPY" to BigDecimal("157.5"),        // Yên Nhật
        "GBP" to BigDecimal("0.79"),         // Bảng Anh
        "AUD" to BigDecimal("1.5"),          // Đô la Úc
        "CAD" to BigDecimal("1.37"),         // Đô la Canada
        "CHF" to BigDecimal("0.9"),          // Franc Thụy Sĩ
        "CNY" to BigDecimal("7.25"),         // Nhân dân tệ Trung Quốc
        "KRW" to BigDecimal("1385.0"),       // Won Hàn Quốc
        "SGD" to BigDecimal("1.35")          // Đô la Singapore
    )

    private val currencies = exchangeRates.keys.toList()

    private lateinit var spinnerFrom: Spinner
    private lateinit var spinnerTo: Spinner
    private lateinit var etFromAmount: EditText
    private lateinit var etToAmount: EditText

    private var isUpdating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Khởi tạo các View
        spinnerFrom = findViewById(R.id.spinner_from_currency)
        spinnerTo = findViewById(R.id.spinner_to_currency)
        etFromAmount = findViewById(R.id.et_from_amount)
        etToAmount = findViewById(R.id.et_to_amount)

        setupSpinners()
        setupTextWatchers()
    }

    private fun setupSpinners() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerFrom.adapter = adapter
        spinnerTo.adapter = adapter

        // Thiết lập giá trị mặc định
        spinnerFrom.setSelection(currencies.indexOf("USD"))
        spinnerTo.setSelection(currencies.indexOf("VND"))

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Khi thay đổi spinner, tính toán lại giá trị
                convertCurrency(etFromAmount, etToAmount, spinnerFrom, spinnerTo)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerFrom.onItemSelectedListener = listener
        spinnerTo.onItemSelectedListener = listener
    }

    private fun setupTextWatchers() {
        // 4. Tự động chuyển đổi khi thay đổi nội dung EditText
        etFromAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 5. Khi EditText này thay đổi, EditText còn lại tự động cập nhật
                if (!isUpdating) {
                    convertCurrency(etFromAmount, etToAmount, spinnerFrom, spinnerTo)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        etToAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isUpdating) {
                    convertCurrency(etToAmount, etFromAmount, spinnerTo, spinnerFrom)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun convertCurrency(
        sourceEditText: EditText,
        targetEditText: EditText,
        sourceSpinner: Spinner,
        targetSpinner: Spinner
    ) {
        isUpdating = true

        try {
            val sourceAmountStr = sourceEditText.text.toString()
            if (sourceAmountStr.isEmpty() || sourceAmountStr == ".") {
                targetEditText.text.clear()
                return
            }

            val sourceAmount = BigDecimal(sourceAmountStr)
            val fromCurrency = sourceSpinner.selectedItem.toString()
            val toCurrency = targetSpinner.selectedItem.toString()

            val fromRate = exchangeRates[fromCurrency]!!
            val toRate = exchangeRates[toCurrency]!!


            val result = sourceAmount.multiply(toRate).divide(fromRate, 2, RoundingMode.HALF_UP)

            targetEditText.setText(result.stripTrailingZeros().toPlainString())

        } catch (e: NumberFormatException) {
            targetEditText.text.clear()
        } finally {
            isUpdating = false
        }
    }
}
