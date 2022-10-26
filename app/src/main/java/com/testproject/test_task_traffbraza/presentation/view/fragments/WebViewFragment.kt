package com.testproject.test_task_traffbraza.presentation.view.fragments

import android.os.Bundle
import androidx.activity.OnBackPressedCallback

class WebViewFragment : BaseWebFragment() {

    var url: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUrl()
        initBackPressed()
    }

    private fun initUrl() {
        url = "https://fex.net/"
    }

    private fun initBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { onBackPressed() }
        })
    }

    override fun url() = url
}