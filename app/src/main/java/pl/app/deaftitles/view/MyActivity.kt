package pl.app.deaftitles.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_deaftitles.*
import kotlinx.coroutines.experimental.launch
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import pl.app.deaftitles.MoviePreview
import pl.app.deaftitles.R
import pl.app.deaftitles.fragment.PermissionsChecker
import pl.app.deaftitles.viewmodel.DeaftitlesViewModel

@SuppressLint("Registered")
open class MyActivity : AppCompatActivity(), PermissionsChecker {

    companion object {
        private const val MY_PERMISSIONS_CAMERA = 0
    }

    private var moviePreview: MoviePreview? = null
        set(value) {
            field = value
            cameraPermissionsMessage(false)
        }

    protected val viewModel: DeaftitlesViewModel by inject {
        parametersOf(this)
    }

    override fun check() {

        if (hasCameraPermissionsGranted()) {
            moviePreview = MoviePreview(cameraTextureView, this@MyActivity)
        } else {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CAMERA),
                    MY_PERMISSIONS_CAMERA)
        }
    }

    private var cameraPermissionsDialog: AlertDialog? = null

    /**
     * Check camera permissions
     */
    private fun hasCameraPermissionsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) true
        else {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }


    /**
     * Should ask user for permission
     */
    private fun shouldRequestForCameraPermissionRationale(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA)
    }

    fun showGoToSettingsDialog() {
        if (cameraPermissionsDialog == null)
            cameraPermissionsDialog = AlertDialog.Builder(this).apply {
                setTitle(getString(R.string.permissions_needed))
                setMessage(getString(R.string.camera_permissions_explanation))
                setPositiveButton(R.string.go_to_settings) { _, _ ->
                    //Go to app settings
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    context?.startActivity(intent)
                }
                setNegativeButton(android.R.string.cancel) { _, _ ->
                    cameraPermissionsMessage(true)
                }
                setCancelable(true)
            }.create()
        if (cameraPermissionsDialog?.isShowing == false) cameraPermissionsDialog?.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    moviePreview = MoviePreview(cameraTextureView, this@MyActivity)
                } else {
                    //Ask about camera permissions
                    if (!shouldRequestForCameraPermissionRationale()) {
                        //Show user explanation
                        showGoToSettingsDialog()
                    } else {
                        cameraPermissionsMessage(true)
                    }
                }
                return
            }
        }
    }

    private fun cameraPermissionsMessage(shouldShow: Boolean) {
        if (shouldShow) warningButton.show() else warningButton.hide()
    }

    private fun String.toast() = Toast.makeText(this@MyActivity, this, Toast.LENGTH_SHORT).show()

    override fun onStart() {
        super.onStart()
        check()
    }

    override fun onStop() {
        super.onStop()
        moviePreview?.onStop()
        //Save moment in background thread
        launch {
            viewModel.saveMoment()
        }
    }

    override fun onResume() {
        super.onResume()
        moviePreview?.onResume()
    }
}