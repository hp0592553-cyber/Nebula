package com.frogobox.webview.ui.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.frogobox.coresdk.util.FrogoConstant
import com.frogobox.sdk.ext.gone
import com.frogobox.sdk.ext.startActivityExtOpenApp
import com.frogobox.sdk.ext.visible
import com.frogobox.webview.ConfigApp
import com.frogobox.webview.common.callback.AdCallback
import com.frogobox.webview.common.core.BaseActivity
import com.frogobox.webview.common.ext.APP_ID
import com.frogobox.webview.databinding.ActivityMainBinding
import com.frogobox.webview.databinding.DialogRatingAppBinding

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private val FILE_CHOOSER_RESULT_CODE = 1
    private val STORAGE_PERMISSION_CODE = 101

    override fun setupViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreateExt(savedInstanceState: Bundle?) {
        super.onCreateExt(savedInstanceState)
        
        checkStoragePermissions()
        setupWebViewSettings()
        
        showUMP(this) {
            setupFlagAd()
        }
        setupFlagAd()
    }

    private fun checkStoragePermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
        }
    }

    private fun setupWebViewSettings() {
        val webSettings = binding.mainWebview.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.allowFileAccess = true
        webSettings.allowContentAccess = true
        
        // HACK PRO: Permite que o WebView abra a galeria do Android
        binding.mainWebview.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                this@MainActivity.filePathCallback = filePathCallback
                val intent = fileChooserParams?.createIntent()
                try {
                    startActivityForResult(intent!!, FILE_CHOOSER_RESULT_CODE)
                } catch (e: Exception) {
                    this@MainActivity.filePathCallback = null
                    return false
                }
                return true
            }
        }
        
        binding.mainWebview.setBackgroundColor(android.graphics.Color.parseColor("#020306"))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (filePathCallback == null) return
            val results = if (data == null || resultCode != Activity.RESULT_OK) null else arrayOf(Uri.parse(data.dataString))
            filePathCallback?.onReceiveValue(results)
            filePathCallback = null
        }
    }

    override fun doOnBackPressedExt() {
        if (binding.mainWebview.canGoBack()) {
            binding.mainWebview.goBack()
        } else {
            showDialog()
        }
    }

    private fun setupFlagAd() {
        if (ConfigApp.Flag.IS_USING_AD_INTERSTITIAL) {
            setupAd()
        } else {
            setupUI()
        }
    }

    private fun setupAd() {
        showInterstitial(object : AdCallback {
            override fun onShowProgress() { binding.containerProgressView.progressView.visible() }
            override fun onHideProgress() { binding.containerProgressView.progressView.gone() }
            override fun onFinish() { setupUI() }
            override fun onFailed() { setupUI() }
        })
    }

    private fun setupLoadWeb() {
        binding.mainWebview.loadUrl("file:///android_asset/index.html")
        binding.containerProgressView.progressView.gone()
        binding.containerFailedView.failedView.gone()
    }

    private fun setupUI() {
        binding.containerFailedView.ivClose.setOnClickListener {
            binding.containerFailedView.failedView.gone()
            setupLoadWeb()
        }
        setupLoadWeb()
    }

    private fun showDialog() {
        val dialogBinding = DialogRatingAppBinding.inflate(layoutInflater, null, false)
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(dialogBinding.root)
        dialogBinding.btnRate.setOnClickListener { rateApp() }
        dialogBinding.btnExit.setOnClickListener { exitApp() }
        dialogBuilder.create().show()
    }

    private fun rateApp() {
        startActivityExtOpenApp("${FrogoConstant.Url.BASE_PLAY_STORE_URL}${APP_ID}")
    }

    private fun exitApp() { finishAffinity() }
}

