package com.frogobox.webview.ui.main

import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import com.frogobox.coresdk.util.FrogoConstant
import com.frogobox.sdk.ext.gone
import com.frogobox.sdk.ext.startActivityExtOpenApp
import com.frogobox.sdk.ext.visible
import com.frogobox.webview.ConfigApp
import com.frogobox.webview.common.callback.AdCallback
import com.frogobox.webview.common.callback.WebViewCallback
import com.frogobox.webview.common.core.BaseActivity
import com.frogobox.webview.common.ext.APP_ID
import com.frogobox.webview.common.ext.loadUrlExt
import com.frogobox.webview.databinding.ActivityMainBinding
import com.frogobox.webview.databinding.DialogRatingAppBinding

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun setupViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreateExt(savedInstanceState: Bundle?) {
        super.onCreateExt(savedInstanceState)
        
        // --- HACK DO RICK: CONFIGURAÇÃO DE ALTA PERFORMANCE ---
        val webSettings = binding.mainWebview.settings
        webSettings.javaScriptEnabled = true // Ativa o coração do Monstro
        webSettings.domStorageEnabled = true // Permite salvar vídeos na memória
        webSettings.allowFileAccess = true
        webSettings.allowContentAccess = true
        webSettings.databaseEnabled = true
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        
        // Garante que o fundo seja preto para evitar clarão branco
        binding.mainWebview.setBackgroundColor(android.graphics.Color.parseColor("#020306"))
        
        showUMP(this) {
            setupFlagAd()
        }
        setupFlagAd()
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
        binding.apply {
            // CARGA DIRETA DO TEU INDEX NA PASTA ASSETS
            mainWebview.loadUrl("file:///android_asset/index.html")
            
            // Callback para animações de carregamento
            containerProgressView.progressView.gone()
            containerFailedView.failedView.gone()
        }
    }

    private fun setupUI() {
        binding.apply {
            containerFailedView.ivClose.setOnClickListener {
                containerFailedView.failedView.gone()
                setupLoadWeb()
            }
            setupLoadWeb()
        }
    }

    private fun showDialog() {
        val dialogBinding = DialogRatingAppBinding.inflate(layoutInflater, null, false)
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(dialogBinding.root)
        dialogBinding.apply {
            btnRate.setOnClickListener { rateApp() }
            btnExit.setOnClickListener { exitApp() }
        }
        dialogBuilder.create().show()
    }

    private fun rateApp() {
        startActivityExtOpenApp("${FrogoConstant.Url.BASE_PLAY_STORE_URL}${APP_ID}")
    }

    private fun exitApp() { finishAffinity() }
}

