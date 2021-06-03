package com.alyxe.images.glide

import androidx.compose.ui.graphics.ImageBitmap

internal sealed class GlideImageState {
    object Loading : GlideImageState()
    class Success(val bitmap: ImageBitmap) : GlideImageState()
    class Failure(val throwable: Throwable?) : GlideImageState()
}