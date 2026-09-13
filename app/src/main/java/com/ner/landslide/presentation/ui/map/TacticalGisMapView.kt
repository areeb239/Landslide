package com.ner.landslide.presentation.ui.map

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.gson.Gson
import com.ner.landslide.domain.model.RiskZone
import com.ner.landslide.domain.model.RoadSegment
import com.ner.landslide.util.SelectedLocation

class AndroidMapBridge(
    private val onZone: (String) -> Unit,
    private val onRoad: (String) -> Unit,
    private val onMapClick: () -> Unit,
    private val onReady: () -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onZoneClicked(zoneId: String) {
        handler.post { onZone(zoneId) }
    }

    @JavascriptInterface
    fun onRoadClicked(roadId: String) {
        handler.post { onRoad(roadId) }
    }

    @JavascriptInterface
    fun onMapClicked() {
        handler.post { onMapClick() }
    }

    @JavascriptInterface
    fun onMapReady() {
        handler.post { onReady() }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TacticalGisMapView(
    modifier: Modifier = Modifier,
    selectedLocation: SelectedLocation?,
    riskZones: List<RiskZone>,
    roadSegments: List<RoadSegment>,
    showRiskLayer: Boolean,
    showRoadLayer: Boolean,
    mapMode: String,
    onZoneClick: (RiskZone) -> Unit,
    onRoadClick: (RoadSegment) -> Unit,
    onMapClick: () -> Unit,
    onWebViewReady: (WebView) -> Unit
) {
    val context = LocalContext.current
    val inlinedHtml = remember(context) {
        try {
            val css = context.assets.open("map/leaflet.css").bufferedReader().use { it.readText() }
            val js = context.assets.open("map/leaflet.js").bufferedReader().use { it.readText() }
            val html = context.assets.open("map/tactical_map.html").bufferedReader().use { it.readText() }
            html.replace("<link rel=\"stylesheet\" href=\"leaflet.css\" />", "<style>\n$css\n</style>")
                .replace("<script src=\"leaflet.js\"></script>", "<script>\n$js\n</script>")
        } catch (e: Exception) {
            Log.e("TacticalMapJS", "Failed to inline map assets: ${e.message}", e)
            ""
        }
    }
    val gson = remember { Gson() }
    var isMapReady by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    fun syncAll(wv: WebView) {
        wv.evaluateJavascript("refreshMap();", null)
        wv.evaluateJavascript("setMapMode('$mapMode');", null)
        selectedLocation?.let {
            val locJson = gson.toJson(it)
            wv.evaluateJavascript("setActiveLocation($locJson); setCenter(${it.latitude}, ${it.longitude}, 10);", null)
        }
        val zonesToSend = if (showRiskLayer) riskZones else emptyList()
        wv.evaluateJavascript("setZones(${gson.toJson(zonesToSend)});", null)
        val roadsToSend = if (showRoadLayer) roadSegments else emptyList()
        wv.evaluateJavascript("setRoads(${gson.toJson(roadsToSend)});", null)
    }

    LaunchedEffect(mapMode, isMapReady) {
        if (isMapReady) {
            webViewRef?.evaluateJavascript("setMapMode('$mapMode');", null)
        }
    }

    LaunchedEffect(selectedLocation, isMapReady) {
        if (isMapReady && selectedLocation != null) {
            val locJson = gson.toJson(selectedLocation)
            webViewRef?.evaluateJavascript("setActiveLocation($locJson); setCenter(${selectedLocation.latitude}, ${selectedLocation.longitude}, 10);", null)
        }
    }

    LaunchedEffect(riskZones, showRiskLayer, isMapReady) {
        if (isMapReady) {
            val zonesToSend = if (showRiskLayer) riskZones else emptyList()
            webViewRef?.evaluateJavascript("setZones(${gson.toJson(zonesToSend)});", null)
        }
    }

    LaunchedEffect(roadSegments, showRoadLayer, isMapReady) {
        if (isMapReady) {
            val roadsToSend = if (showRoadLayer) roadSegments else emptyList()
            webViewRef?.evaluateJavascript("setRoads(${gson.toJson(roadsToSend)});", null)
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
                WebView.setWebContentsDebuggingEnabled(true)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.allowFileAccessFromFileURLs = true
                settings.allowUniversalAccessFromFileURLs = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        Log.d("TacticalMapJS", "CONSOLE: [${consoleMessage?.messageLevel()}] ${consoleMessage?.message()} (${consoleMessage?.sourceId()}:${consoleMessage?.lineNumber()})")
                        return true
                    }
                }

                val bridge = AndroidMapBridge(
                    onZone = { zoneId ->
                        riskZones.firstOrNull { it.id == zoneId }?.let(onZoneClick)
                    },
                    onRoad = { roadId ->
                        roadSegments.firstOrNull { it.id == roadId }?.let(onRoadClick)
                    },
                    onMapClick = onMapClick,
                    onReady = {
                        Log.d("TacticalMapJS", "Bridge: onMapReady received from JS!")
                        isMapReady = true
                        syncAll(this@apply)
                    }
                )
                addJavascriptInterface(bridge, "AndroidBridge")

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        Log.d("TacticalMapJS", "onPageStarted: $url")
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Log.d("TacticalMapJS", "onPageFinished: $url")
                        isMapReady = true
                        syncAll(this@apply)
                        view?.postDelayed({
                            view.evaluateJavascript("if (window.resizeMap) window.resizeMap();", null)
                        }, 100)
                        view?.postDelayed({
                            view.evaluateJavascript("if (window.resizeMap) window.resizeMap();", null)
                        }, 400)
                    }

                    override fun onReceivedError(view: WebView?, request: android.webkit.WebResourceRequest?, error: android.webkit.WebResourceError?) {
                        super.onReceivedError(view, request, error)
                        Log.e("TacticalMapJS", "onReceivedError: ${error?.description} (${error?.errorCode}) on ${request?.url}")
                    }

                    override fun onReceivedHttpError(view: WebView?, request: android.webkit.WebResourceRequest?, errorResponse: android.webkit.WebResourceResponse?) {
                        super.onReceivedHttpError(view, request, errorResponse)
                        Log.e("TacticalMapJS", "onReceivedHttpError: ${errorResponse?.statusCode} on ${request?.url}")
                    }
                }

                Log.d("TacticalMapJS", "Calling loadDataWithBaseURL(https://bhoochetak.local/, ...)")
                loadDataWithBaseURL(
                    "https://bhoochetak.local/",
                    inlinedHtml,
                    "text/html",
                    "UTF-8",
                    null
                )
                webViewRef = this
                onWebViewReady(this)
            }
        },
        update = { wv ->
            if (isMapReady) {
                syncAll(wv)
            }
        }
    )
}
