@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.dertyp7214.apkmirror.screens

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.dertyp7214.apkmirror.R
import com.dertyp7214.apkmirror.common.*
import com.dertyp7214.apkmirror.common.NetworkTools.Companion.drawableFromUrl
import com.dertyp7214.themeablecomponents.utils.ThemeManager
import kotlinx.android.synthetic.main.activity_app_data_screen.*

class AppDataScreen : AppCompatActivity() {

    private lateinit var htmlParser: HtmlParser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_data_screen)
        setSupportActionBar(toolbar)

        val themeManager = ThemeManager.getInstance(this)
        themeManager.enableStatusAndNavBar(this)

        if (intent == null || intent.extras == null || !intent.extras.containsKey("url")) finish()
        val extras = intent.extras
        htmlParser = HtmlParser(this)
        val appData = Adapter.apps[extras["url"]]
        Adapter.progressDialog?.dismiss()
        title = ""
        txt_title.text = appData!!.app.title
        txt_title.setTextColor(if (themeManager.darkMode) Color.WHITE else Color.BLACK)

        icon.setImageDrawable(drawableFromUrl(this, appData.app.imageUrl))

        txt_description.setLinkTextColor(themeManager.colorAccent)
        appData.applyDescriptionToTextView(txt_description)

        val variantAdapter = VariantAdapter(this, appData.variants)
        val variantBottomSheet = BottomSheet(getString(R.string.titleVariants), variantAdapter)
        btn_vars.visibility = if (appData.variants.size > 0) View.VISIBLE else View.INVISIBLE
        btn_vars.setOnClickListener {
            variantBottomSheet.show(supportFragmentManager, "Variants")
        }

        val versionAdapter = VersionAdapter(this, appData.versions)
        val versionBottomSheet = BottomSheet(getString(R.string.titleVersions), versionAdapter)
        btn_vers.visibility = if (appData.versions.size > 0) View.VISIBLE else View.INVISIBLE
        btn_vers.setOnClickListener {
            versionBottomSheet.show(supportFragmentManager, "Versions")
        }
    }
}
