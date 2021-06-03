package com.alyxe.images

import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.core.net.toUri

sealed class TargetResource {
    class Local(@RawRes @DrawableRes val id: Int) : TargetResource()

    class AndroidDrawable(val drawable: Drawable) : TargetResource()

    class File(val uri: Uri) : TargetResource() {
        constructor(urlString: String) : this(urlString.toUri())
    }
}