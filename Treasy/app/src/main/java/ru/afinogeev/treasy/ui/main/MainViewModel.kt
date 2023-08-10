package ru.afinogeev.treasy.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.afinogeev.treasy.util.SmoothedMutableLiveData
import ru.afinogeev.treasy.ui.main.MainFragment.Companion.DESIRED_HEIGHT_CROP_PERCENT
import ru.afinogeev.treasy.ui.main.MainFragment.Companion.DESIRED_WIDTH_CROP_PERCENT

class MainViewModel : ViewModel() {

    companion object {
        // Amount of time (in milliseconds) to wait for detected text to settle
        private const val SMOOTHING_DURATION = 50L
    }

    var sourceText = SmoothedMutableLiveData<String>(SMOOTHING_DURATION)


    // We set desired crop percentages to avoid having to analyze the whole image from the live
    // camera feed. However, we are not guaranteed what aspect ratio we will get from the camera, so
    // we use the first frame we get back from the camera to update these crop percentages based on
    // the actual aspect ratio of images.
    val imageCropPercentages = MutableLiveData<Pair<Int, Int>>()
        .apply { value = Pair(DESIRED_HEIGHT_CROP_PERCENT, DESIRED_WIDTH_CROP_PERCENT) }

}