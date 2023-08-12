package com.jonesandjay123.wheelofchoices

import android.app.Activity
import android.os.Bundle
import com.jonesandjay123.wheelofchoices.databinding.ActivityMainBinding

class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}