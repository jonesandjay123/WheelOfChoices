package com.jonesandjay123.wheelofchoices

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
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
