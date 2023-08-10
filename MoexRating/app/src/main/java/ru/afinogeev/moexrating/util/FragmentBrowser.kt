package ru.afinogeev.moexrating.util

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment


interface FragmentBrowser {
    fun openBrowser(fragment:Fragment, link:String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        fragment.startActivity(browserIntent)
    }
}