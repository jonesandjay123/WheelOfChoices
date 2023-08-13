package com.jonesandjay123.wheelofchoices

import android.animation.ValueAnimator
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
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jonesandjay123.wheelofchoices.databinding.ActivityMainBinding
import java.util.Random
import kotlin.math.abs

class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding

    private fun saveOptions(options: List<String>) {
        val sharedPreferences = getSharedPreferences("WheelOptions", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("size", options.size)
        for (i in options.indices) {
            editor.putString("option_$i", options[i])
        }
        editor.apply()
    }

    private fun loadOptions(): MutableList<String> {
        val sharedPreferences = getSharedPreferences("WheelOptions", Context.MODE_PRIVATE)
        val size = sharedPreferences.getInt("size", 0)
        val options = mutableListOf<String>()
        for (i in 0 until size) {
            options.add(sharedPreferences.getString("option_$i", "") ?: "")
        }
        return options
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 從 SharedPreferences 加載選項
        val options = loadOptions()
        if (options.isEmpty()) {
            options.addAll(listOf("Option A", "Option B")) // 如果沒有保存的選項，則使用默認選項
        }

        binding.buttonAdd.setOnClickListener {
            val newOption = binding.editTextOption.text.toString()
            options.add(newOption)
            saveOptions(options) // 保存選項
            binding.wheelView.setOptions(options)
            binding.recyclerView.adapter = OptionsAdapter(options) { selectedOption ->
                binding.editTextOption.setText(selectedOption)
            } // 重新設置適配器
        }

        binding.buttonRemove.setOnClickListener {
            val optionToRemove = binding.editTextOption.text.toString()
            options.remove(optionToRemove)
            saveOptions(options) // 保存選項
            binding.wheelView.setOptions(options)
            binding.recyclerView.adapter = OptionsAdapter(options) { selectedOption ->
                binding.editTextOption.setText(selectedOption)
            } // 重新設置適配器
        }

        // 設置RecyclerView的佈局管理器和適配器
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = OptionsAdapter(options) { selectedOption ->
            binding.editTextOption.setText(selectedOption)
        }

        // 使用 binding 訪問 WheelView
        val wheelView = binding.wheelView
        // 設置 WheelView 的選項
        wheelView.setOptions(options)

        binding.toggleChanceOrFate.isChecked = true // 預設為Fate模式

        binding.toggleChanceOrFate.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.wheelView.mode = Mode.Fate
            } else {
                binding.wheelView.mode = Mode.Chance
            }
        }
    }
}

class OptionsAdapter(private val options: List<String>, private val onItemClick: (String) -> Unit) :
    RecyclerView.Adapter<OptionsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val optionTextView: TextView = view.findViewById(R.id.option_text_view)
        fun bind(option: String) {
            optionTextView.text = option
            itemView.setOnClickListener {
                onItemClick(option)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.option_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(options[position])
    }

    override fun getItemCount() = options.size
}

enum class Mode {
    Fate, Chance
}

class WheelView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    var mode: Mode = Mode.Fate // 預設模式為Fate
        set(value) { // 自定義setter
            field = value // 更新屬性值
            if (value == Mode.Chance && animator.isRunning) {
                animator.cancel() // 如果更改為Chance模式且轉盤正在旋轉，則立即停止
            }
        }

    private val chanceRotationSpeed = 10f // 這個值定義了"Chance"模式下的轉速

    private val wheelRadius = 135f // 轉盤的半徑
    private val pointerRadius = 10f // 指針的半徑
    private var options: List<String> = listOf() // 轉盤上的選項
    private val colors = mutableListOf<Int>()
    private var currentAngle = 0f // 轉盤的當前角度
    private var longPressStartTime: Long = 0
    private var isLongPress: Boolean = false

    // 繪製轉盤的畫筆
    private val wheelPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // 繪製指針的畫筆
    private val pointerPaint = Paint().apply {
        color = Color.parseColor("#FFC107")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // 繪製文字的畫筆
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 42f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    fun setOptions(newOptions: List<String>) {
        options = newOptions
        setRandomColors(newOptions.size)
        invalidate() // 重新繪製視圖以反映新的選項
    }

    private fun setRandomColors(size: Int) {
        colors.clear()
        if (size == 0) {
            colors.add(Color.WHITE)
            return
        }
        val random = Random()
        for (i in 0 until size) {
            val color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
            colors.add(color)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val optionAngle = 360f / options.size

        for (i in options.indices) {
            wheelPaint.color = colors.getOrElse(i) { Color.WHITE }
            val startAngle = i * optionAngle + currentAngle
            canvas.drawArc(
                centerX - wheelRadius,
                centerY - wheelRadius,
                centerX + wheelRadius,
                centerY + wheelRadius,
                startAngle,
                optionAngle,
                true,
                wheelPaint
            )

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

        canvas.drawCircle(centerX, centerY, pointerRadius, pointerPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                longPressStartTime = System.currentTimeMillis()
                if (mode == Mode.Chance && animator.isRunning) {
                    animator.cancel() // 如果是Chance模式且轉盤正在旋轉，則立即停止
                }
            }
            MotionEvent.ACTION_UP -> {
                if (System.currentTimeMillis() - longPressStartTime > 1000) {
                    isLongPress = true
                    if (mode == Mode.Fate) {
                        startSpinningBasedOnLongPressTime(System.currentTimeMillis() - longPressStartTime)
                    } else {
                        startSpinningWithFixedRate() // Chance模式下以固定速度旋轉
                    }
                }
            }
        }
        return true
    }

    private val animator = ValueAnimator.ofFloat(chanceRotationSpeed, chanceRotationSpeed) // 使用固定的轉速

    private fun startSpinningWithFixedRate() {
        animator.removeAllUpdateListeners() // 清除之前的所有更新監聽器以避免隨回合累加的轉速

        animator.duration = Long.MAX_VALUE // 永遠不停止
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener { animation ->
            currentAngle += animation.animatedValue as Float
            currentAngle %= 360 // 確保角度在0到360之間
            invalidate()
        }
        animator.start()
        isLongPress = false
    }

    private fun startSpinningBasedOnLongPressTime(pressDuration: Long) {
        val rotationSpeed = if (mode == Mode.Fate) {
            pressDuration.toFloat() / 100 // Fate模式下的旋轉速度計算
        } else {
            chanceRotationSpeed // Chance模式下使用固定的轉速
        }
        val percentageOfVelocity = 0.3f // 使用速度的30%來計算持續時間，你可以調整這個值
        val animator = ValueAnimator.ofFloat(rotationSpeed, 0f)
        val factor = 2.0f
        animator.duration = abs((rotationSpeed * 2000 * percentageOfVelocity).toLong()) // 可以根據需要調整
        animator.interpolator = DecelerateInterpolator(factor)
        animator.addUpdateListener { animation ->
            currentAngle += animation.animatedValue as Float
            invalidate()
        }
        animator.start()
        isLongPress = false
    }
}

