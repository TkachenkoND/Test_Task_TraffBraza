package com.testproject.test_task_traffbraza.presentation.view.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.view.*
import android.webkit.*
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.testproject.test_task_traffbraza.R

abstract class BaseWebFragment : Fragment() {
    private var extraHeaders: MutableMap<String, String> = HashMap()
    private lateinit var valueCallback: ValueCallback<Array<Uri?>>
    private lateinit var container: FrameLayout
    private lateinit var progress: ProgressBar
    private lateinit var webView: WebView
    private var secondaryWebView: WebView? = null

    abstract fun url(): String?

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onDestroyView() {
        super.onDestroyView()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(
        inflater: LayoutInflater, group: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        extraHeaders["X-Requested-With"] = "app-view"

        return context?.let {
            container = FrameLayout(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            progress = ProgressBar(it).apply {
                val size = 42.px
                layoutParams = FrameLayout.LayoutParams(size, size).apply {
                    gravity = Gravity.CENTER
                }
            }

            progress.indeterminateDrawable
                .setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.purple_200),
                    PorterDuff.Mode.SRC_IN
                )

            webView = WebView(it).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                visibility = View.GONE
            }

            listOf(webView, progress).forEach { v -> container.addView(v) }

            container
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        initWebView()

        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        if (savedInstanceState == null) {
            webView.loadUrl(url() ?: "https://google.com")
        } else {
            webView.restoreState(savedInstanceState)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        webView.apply {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            this.webViewClient = object : WebViewClient() {

                override fun onPageFinished(layout: WebView?, webURL: String?) {
                    super.onPageFinished(layout, webURL)

                    visibility = View.VISIBLE
                    this@BaseWebFragment.progress.visibility = View.GONE
                }

                override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                    return true
                }
            }

            this.webChromeClient = object : WebChromeClient() {
                override fun onShowFileChooser(
                    layout: WebView?, fpcBack: ValueCallback<Array<Uri?>>,
                    fcpChooser: FileChooserParams?
                ): Boolean {
                    valueCallback = fpcBack

                    startFileChooser()

                    return true
                }

                override fun onCloseWindow(window: WebView?) {
                    super.onCloseWindow(window)

                    secondaryWebView?.destroy()
                    container.removeView(secondaryWebView)
                    visibility = View.VISIBLE
                }

                override fun onCreateWindow(
                    view: WebView?, isMessage: Boolean,
                    isPlayerGest: Boolean, message: Message?
                ): Boolean {
                    val go = message?.obj as WebView.WebViewTransport?

                    secondaryWebView = WebView(context).apply {
                        setLayerType(View.LAYER_TYPE_HARDWARE, null)

                        this.webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                url: String
                            ): Boolean {
                                view?.loadUrl(url)

                                return true
                            }
                        }

                        setJSSettings()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && secondaryWebView != null)
                            CookieManager.getInstance()
                                .setAcceptThirdPartyCookies(secondaryWebView, true)
                        else
                            CookieManager.getInstance().setAcceptCookie(true)
                    }

                    container.addView(secondaryWebView)
                    visibility = View.GONE
                    go?.webView = secondaryWebView
                    message?.sendToTarget()

                    return true
                }
            }

            setJSSettings()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
            else
                CookieManager.getInstance().setAcceptCookie(true)
        }
    }

    fun onBackPressed() {
        when {
            secondaryWebView?.visibility == View.VISIBLE -> {
                if (secondaryWebView?.canGoBack() == true)
                    secondaryWebView?.goBack()
                else {
                    secondaryWebView?.destroy()
                    container.removeView(secondaryWebView)
                    webView.visibility = View.VISIBLE
                }
            }
            webView.canGoBack() -> {
                val list = webView.copyBackForwardList()

                if (list.currentIndex == 1) {
                } else {
                    webView.goBack()
                }
            }

        }

    }

    @SuppressLint("SetJavaScriptEnabled")
    fun WebView.setJSSettings() {
        settings.apply {
            javaScriptCanOpenWindowsAutomatically = true
            javaScriptEnabled = true
            domStorageEnabled = true
            setSupportMultipleWindows(true)
            useWideViewPort = true

            userAgentString = userAgentString.replaceAfter(")", "")
        }

        requestFocus(View.FOCUS_DOWN)
    }


    fun activityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == RC_FILE_CHOOSER) {
            val final =
                if (intent == null || resultCode != Activity.RESULT_OK)
                    null
                else
                    intent.data

            if (final != null)
                valueCallback.onReceiveValue(arrayOf(final))
        }
    }


    @Suppress("DEPRECATION")
    private fun startFileChooser() {
        val intTargetActivity = Intent(Intent.ACTION_GET_CONTENT)

        intTargetActivity.addCategory(Intent.CATEGORY_OPENABLE)

        intTargetActivity.type = "image/*"

        startActivityForResult(
            Intent.createChooser(intTargetActivity, "Image Chooser"),
            RC_FILE_CHOOSER
        )
    }

    companion object {
        private const val RC_FILE_CHOOSER = 333
    }
}

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()