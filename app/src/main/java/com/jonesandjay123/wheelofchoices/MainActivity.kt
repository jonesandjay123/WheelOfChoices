package com.jonesandjay123.wheelofchoices

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jonesandjay123.wheelofchoices.databinding.ActivityMainBinding

class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 這些是你的選項
        val options = listOf("Option 1", "Option 2", "Option 3")

        // 設置RecyclerView的佈局管理器和適配器
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = OptionsAdapter(options)
    }
}

class OptionsAdapter(private val options: List<String>) :
    RecyclerView.Adapter<OptionsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val optionTextView: TextView = itemView.findViewById(R.id.option_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.option_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.optionTextView.text = options[position]
    }

    override fun getItemCount() = options.size
}

class WheelView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    // 轉盤的半徑
    private val wheelRadius = 100f

    // 指針的半徑
    private val pointerRadius = 10f

    // 轉盤的顏色
    private val wheelColor = Color.parseColor("#E91E63")

    // 指針的顏色
    private val pointerColor = Color.parseColor("#FFC107")

    // 轉盤上的選項
    private val options = arrayOf("Option 1", "Option 2", "Option 3", "Option 4")

    // 轉盤的當前角度
    private var currentAngle = 0f

    // 繪製轉盤的畫筆
    private val wheelPaint = Paint().apply {
        color = wheelColor
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // 繪製指針的畫筆
    private val pointerPaint = Paint().apply {
        color = pointerColor
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // 繪製文字的畫筆
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 20f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 繪製轉盤
        val centerX = width / 2f
        val centerY = height / 2f
        canvas.drawCircle(centerX, centerY, wheelRadius, wheelPaint)

        // 繪製轉盤上的選項
        val optionAngle = 360f / options.size
        for (i in options.indices) {
            val startAngle = i * optionAngle + currentAngle
            val path = Path()
            path.addArc(
                centerX - wheelRadius,
                centerY - wheelRadius,
                centerX + wheelRadius,
                centerY + wheelRadius,
                startAngle,
                optionAngle
            )
            canvas.drawTextOnPath(options[i], path, 0f, 0f, textPaint)
        }

        // 繪製指針
        canvas.drawCircle(centerX, centerY, pointerRadius, pointerPaint)

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 在這裡我們將處理轉盤的觸摸事件
        return super.onTouchEvent(event)
    }
}

