package com.testproject.test_task_traffbraza.presentation.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.onesignal.OneSignal
import com.testproject.test_task_traffbraza.R
import com.testproject.test_task_traffbraza.databinding.ActivityMainBinding
import com.testproject.test_task_traffbraza.presentation.view.fragments.WebViewFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initOneSignal()
        initAppsFlyer()

        supportFragmentManager.commit {
            replace(R.id.containerFragment, WebViewFragment())
        }
    }

    private fun initOneSignal() {
        OneSignal.initWithContext(this)
        OneSignal.setAppId("6748e01b-c51e-4697-aa79-82b6ca3c6dca")
    }

    private fun initAppsFlyer() {
        val callback = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(data: Map<String, Any>) {

            }

            override fun onConversionDataFail(dataFail: String) {

            }

            override fun onAppOpenAttribution(openAttributio: Map<String, String>) {

            }

            override fun onAttributionFailure(attributionFailure: String) {

            }
        }

        AppsFlyerLib.getInstance().init(
            "9Zh7ArqVUYKZJvdtqPhq6m",
            callback,
            this
        )
        AppsFlyerLib.getInstance().start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        WebViewFragment().activityResult(requestCode, resultCode, data)
    }

}