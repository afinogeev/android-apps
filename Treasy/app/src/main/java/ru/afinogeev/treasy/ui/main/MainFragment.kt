package ru.afinogeev.treasy.ui.main


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ru.afinogeev.treasy.analyzer.TextAnalyzer
import ru.afinogeev.treasy.databinding.MainFragmentBinding
import ru.afinogeev.treasy.util.ScopedExecutor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.*
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
//import com.google.android.gms.ads.AdRequest
//import com.google.android.gms.ads.MobileAds
import android.content.Context.MODE_PRIVATE


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()

        // We only need to analyze the part of the image that has text, so we set crop percentages
        // to avoid analyze the entire image from the live camera feed.
        const val DESIRED_WIDTH_CROP_PERCENT = 0
        const val DESIRED_HEIGHT_CROP_PERCENT = 80

        // This is an arbitrary number we are using to keep tab of the permission
        // request. Where an app has multiple context for requesting permission,
        // this can help differentiate the different contexts
        private const val REQUEST_CODE_PERMISSIONS = 10
        // This is an array of all the permission specified in the manifest
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE//,
//            Manifest.permission.SEND_SMS
        )
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
        private const val TAG = "MainFragment"
    }

    private val viewModel: MainViewModel by viewModels()

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private lateinit var container: ConstraintLayout

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var scopedExecutor: ScopedExecutor

    private var _binding: MainFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = MainFragmentBinding.inflate(inflater, container, false)

        val metrics:DisplayMetrics = resources.displayMetrics
        binding.viewFinder.layoutParams.height = (metrics.heightPixels * (1 - (DESIRED_HEIGHT_CROP_PERCENT / 100f))).roundToInt()

        // Ads
//        MobileAds.initialize(context) {}
//        val adRequest = AdRequest.Builder().build()
//        binding.adView.loadAd(adRequest)

        //First run
        val sharedPref = activity?.getSharedPreferences("PREFERENCE",MODE_PRIVATE)
        val isFirstRun:Boolean = sharedPref?.getBoolean("isFirstRun",true) == true
        if (isFirstRun){
            with (sharedPref?.edit()) {
                this?.putBoolean("isFirstRun", false)
                this?.apply()
            }
            val frDialog = FirstRunDialogFragment()
            frDialog.show(
                childFragmentManager, "")
        }


//        binding.sendMoneySms.setOnClickListener(){
//            sendMoneySms()
//        }

        binding.sendMoneyApp.setOnClickListener(){
            val uS = "android-app://ru.sberbankmobile/android-app/ru.sberbankmobile/payments/p2p?type=phone_number"
            sendMoneyApp(uS)
        }

        binding.sendMoneyAppQps.setOnClickListener(){
            if (makeSendStr("app").length == 10){
                val uS = "android-app://ru.sberbankmobile/android-app/ru.sberbankmobile/transfers/qpsTransfer/"
                sendMoneyApp(uS)
            }
            else{
                Toast.makeText(
                    context,
                    "Перевод СБП только по номеру телефона",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

        if(!checkUri())
            checkClipboard()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // Shut down our background executor
        cameraExecutor.shutdown()
        scopedExecutor.shutdown()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        container = view as ConstraintLayout

        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()
        scopedExecutor = ScopedExecutor(cameraExecutor)

        // Request camera permissions
        if (allPermissionsGranted()) {
            // Wait for the views to be properly laid out
            binding.viewFinder.post {
                // Set up the camera and its use cases
                setUpCamera()
            }
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

    }

    override fun onResume() {
        super.onResume()

        if(!checkUri())
            checkClipboard()
    }

    private fun checkClipboard():Boolean {
        //Fill nums from clipboard
        val myClipboard: ClipboardManager = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        if (myClipboard.hasPrimaryClip()) {
            val numStr = myClipboard.primaryClip?.getItemAt(0)?.text.toString().filter { it.isDigit() }
            when(numStr.length){
                10 -> {
                    fillNum(numStr)
                    return true
                }
                11 -> {
                    fillNum(numStr.substring(1,11))
                    return true
                }
                16 -> {
                    fillNum(numStr)
                    return true
                }
            }
        }
        return false
    }

    private fun checkUri(): Boolean {
        //Fill nums from uri
        val intent = activity?.intent
        if (intent != null) {
            if(intent.data!=null) {
                val numStr = intent.data.toString().filter { it.isDigit() }
                when(numStr.length){
                    10 -> {
                        fillNum(numStr)
                        return true
                    }
                    11 -> {
                        fillNum(numStr.substring(1,11))
                        return true
                    }
                    16 -> {
                        fillNum(numStr)
                        return true
                    }
                }
            }
        }
        return false
    }

    /** Initialize CameraX, and prepare to bind the camera use cases  */
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            // CameraProvider
            cameraProvider = cameraProviderFuture.get()

            // Build and bind the camera use cases
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        // Get screen metrics used to setup camera for full screen resolution
        val metrics = DisplayMetrics().also { binding.viewFinder.display.getRealMetrics(it) }
        //Log.d(TAG, "Screen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")

        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        //Log.d(TAG, "Preview aspect ratio: $screenAspectRatio")

        val rotation = binding.viewFinder.display.rotation

        val preview = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()
            .also{
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }


        // Build the image analysis use case and instantiate our analyzer
        imageAnalyzer = ImageAnalysis.Builder()
            // We request aspect ratio but no resolution
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(
                    cameraExecutor
                    , TextAnalyzer(
                        requireContext(),
                        lifecycle,
                        viewModel.sourceText,
                        viewModel.imageCropPercentages
                    )
                )
            }

        viewModel.sourceText.observe(
            viewLifecycleOwner,
            {
                fillNum(it)
            }
        )

        // Select back camera since text detection does not work with front camera
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        try {
            // Unbind use cases before rebinding
            cameraProvider.unbindAll()

            // Bind use cases to camera
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )

        } catch (exc: IllegalStateException) {
            //Log.e(TAG, "Use case binding failed. This must be running on main thread.", exc)
        }
    }

    private fun fillNum(text:String){
        when (text.length) {
            16 -> {
                binding.num1.setText(text.substring(0,4))
                binding.num2.setText(text.substring(4,8))
                binding.num3.setText(text.substring(8,12))
                binding.num4.setText(text.substring(12,16))
            }
            10 -> {
                binding.num1.setText(text.substring(0,3))
                binding.num2.setText(text.substring(3,6))
                binding.num3.setText(text.substring(6,8))
                binding.num4.setText(text.substring(8,10))
            }
        }
    }

    private fun makeSendStr(type:String):String{
        val sb = StringBuilder()
        when(type) {
//            "sms" -> {
//                sb.append("Перевод ")
//                    .append(binding.num1.text.toString())
//                    .append(binding.num2.text.toString())
//                    .append(binding.num3.text.toString())
//                    .append(binding.num4.text.toString())
//                    .append(" ")
//                    .append(binding.price.text.toString())
//            }
            "app" -> {
                sb.append(binding.num1.text.toString())
                    .append(binding.num2.text.toString())
                    .append(binding.num3.text.toString())
                    .append(binding.num4.text.toString())
            }
        }
        return sb.toString()
    }

//    private fun sendMoneySms(){
//
//        val sendStr = makeSendStr("sms")
//
//        try {
//            val sms = SmsManager.getDefault()
//            sms.sendTextMessage("900",null,sendStr,null,null)
//            Toast.makeText(
//                context,
//                sendStr,
//                Toast.LENGTH_SHORT
//            ).show()
//        } catch (exc: IllegalStateException){
//            Toast.makeText(
//                context,
//                "error",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//    }

    private fun sendMoneyApp(uS:String){

        val sendStr = makeSendStr("app")

        try {
            //val sms = SmsManager.getDefault()
            //sms.sendTextMessage("900",null,sendStr,null,null)

            val myClipboard: ClipboardManager = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

            val myClip: ClipData = ClipData.newPlainText("", sendStr)
            myClipboard.setPrimaryClip(myClip)

            Toast.makeText(
                context,
                sendStr,
                Toast.LENGTH_SHORT
            ).show()

            val sbIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uS))

            startActivity(sbIntent)

        } catch (exc: IllegalStateException){
            Toast.makeText(
                context,
                "Ошибка. На данный момент приложение работает с предустановленным приложением 'Сбербанк'",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = ln(max(width, height).toDouble() / min(width, height))
        if (abs(previewRatio - ln(RATIO_4_3_VALUE))
            <= abs(previewRatio - ln(RATIO_16_9_VALUE))
        ) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }


    /**
     * Process result from permission request dialog box, has the request
     * been granted? If yes, start Camera. Otherwise display a toast
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                binding.viewFinder.post {
                    // Set up the camera and its use cases
                    setUpCamera()
                }
            } else {
                Toast.makeText(
                    context,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

}