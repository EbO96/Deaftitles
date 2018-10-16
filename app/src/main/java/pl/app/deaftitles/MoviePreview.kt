package pl.app.deaftitles

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Handler
import android.os.HandlerThread
import android.support.v7.app.AppCompatActivity
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import java.util.*

/**
 * Class used to setup camera preview
 * @property textureView is view where camera can display preview
 */
class MoviePreview(private val textureView: TextureView, private val activity: AppCompatActivity) : ActivityLifecycle {

    private var camera: CameraDevice? = null
    private lateinit var cameraId: String
    private val cameraManager: CameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null
    private var captureRequestBuilder: CaptureRequest.Builder? = null
    private var captureRequest: CaptureRequest? = null
    private var cameraCaptureSession: CameraCaptureSession? = null

    private lateinit var surfaceTextureSize: Size


    /**
     * [TextureView.SurfaceTextureListener] handles several lifecycle events on a
     * [TextureView].
     */
    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture?, width: Int, height: Int) {
            configureTransform(width, height)
        }

        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture?) {

        }

        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture?): Boolean = true

        override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture?, width: Int, height: Int) {
            surfaceTextureSize = Size(width, height)
            setupCamera()
            configureTransform(width, height)
            openCamera()
        }
    }

    private val cameraOpenStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(cameraDevice: CameraDevice?) {
            this@MoviePreview.camera = cameraDevice
            createPreviewSession()
        }

        override fun onDisconnected(cameraDevice: CameraDevice?) {
            closeCamera()
        }

        override fun onError(cameraDevice: CameraDevice?, error: Int) {
            closeCamera()
        }

    }

    private fun createPreviewSession() {
        textureView.surfaceTexture?.apply {
            try {
                setDefaultBufferSize(previewSize.width, previewSize.height)
                val previewSurface = Surface(this)
                captureRequestBuilder = camera?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)?.also {
                    it.addTarget(previewSurface)
                }

                camera?.createCaptureSession(Collections.singletonList(previewSurface),
                        object : CameraCaptureSession.StateCallback() {
                            override fun onConfigured(cameraCaptureSession: CameraCaptureSession?) {
                                if (camera == null) return

                                try {
                                    captureRequest = captureRequestBuilder?.build()
                                    this@MoviePreview.cameraCaptureSession = cameraCaptureSession?.also {
                                        it.setRepeatingRequest(captureRequest, null, backgroundHandler)
                                    }
                                } catch (e: CameraAccessException) {
                                    e.printStackTrace()
                                }
                            }

                            override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession?) {

                            }

                        }, backgroundHandler)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * The [android.util.Size] of camera preview.
     */
    private lateinit var previewSize: Size

    private fun configureTransform(width: Int, height: Int) {
        val rotation = activity.windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val bufferRect = RectF(0f, 0f, previewSize.height.toFloat(), previewSize.width.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()

        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale = Math.max(
                    width.toFloat() / previewSize.height,
                    height.toFloat() / previewSize.width)
            with(matrix) {
//                postScale(scale, scale, centerX, centerY)
                postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
            }
        }
        textureView.setTransform(matrix)

    }

    /**
     * Find back camera
     */
    private fun setupCamera() {
        try {
            cameraManager.cameraIdList.forEach { id ->
                val characteristic = cameraManager.getCameraCharacteristics(id)
                if (characteristic.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    val configurationMap = characteristic.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    val sizes = configurationMap.getOutputSizes(SurfaceTexture::class.java)
                    previewSize = sizes[0]
                    this@MoviePreview.cameraId = id
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        try {
            cameraManager.openCamera(cameraId, cameraOpenStateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun openBackgroundThread() {
        backgroundThread = HandlerThread("camera_background_thread").apply { start() }
        backgroundHandler = Handler(backgroundThread?.looper)
    }

    private fun closeBackgroundThread() {
        backgroundThread?.quitSafely()
        backgroundThread = null
        backgroundHandler = null
    }

    private fun closeCamera() {
        cameraCaptureSession?.close()
        cameraCaptureSession = null

        camera?.close()
        camera = null
    }

    override fun onStop() {
        closeCamera()
        closeBackgroundThread()
    }

    override fun onResume() {
        openBackgroundThread()
        if (textureView.isAvailable) {
            setupCamera()
            openCamera()
        } else {
            textureView.surfaceTextureListener = surfaceTextureListener
        }
    }
}