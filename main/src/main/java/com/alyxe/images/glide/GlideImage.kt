package com.alyxe.images.glide

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import com.alyxe.images.TargetResource
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

private fun constructGlide(context: Context, resource: TargetResource): RequestBuilder<Drawable> {
    val glide = Glide.with(context)
    return when (resource) {
        is TargetResource.AndroidDrawable -> glide.load(resource.drawable)
        is TargetResource.File -> glide.load(resource.uri)
        is TargetResource.Local -> glide.load(resource.id)
    }
}

@Composable
fun BaseGlideImage(
    resource: TargetResource,
    onLoading: @Composable () -> Unit,
    onFailure: @Composable (Throwable?) -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
    requestBuilder: (RequestBuilder<Drawable>.() -> Unit)? = null,
) {
    var imageState by remember { mutableStateOf<GlideImageState>(GlideImageState.Loading) }

    constructGlide(LocalContext.current, resource)
        .let {
            if (requestBuilder != null)
                it.apply(requestBuilder)
            else
                it
        }
        .addListener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                imageState = GlideImageState.Failure(e)
                return true
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                imageState = resource
                    ?.toBitmap()
                    ?.asImageBitmap()
                    ?.let { GlideImageState.Success(it) }
                    ?: return false

                return true
            }
        })
        .preload()

    when (val state = imageState) {
        is GlideImageState.Failure -> onFailure(state.throwable)
        GlideImageState.Loading -> onLoading()
        is GlideImageState.Success -> {
            Image(
                bitmap = state.bitmap,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier,
            )
        }
    }
}

@Composable
fun GlideImage(
    resource: TargetResource,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
    requestBuilder: (RequestBuilder<Drawable>.() -> Unit)? = null,
) = BaseGlideImage(
    resource = resource,
    onLoading = {},
    onFailure = {},
    modifier = modifier,
    contentDescription = contentDescription,
    contentScale = contentScale,
    requestBuilder = requestBuilder
)

@Composable
fun GlideImage(
    resource: TargetResource?,
    alt: TargetResource,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
    onLoading: @Composable () -> Unit = {},
    requestBuilder: (RequestBuilder<Drawable>.() -> Unit)? = null,
) = BaseGlideImage(
    resource = resource ?: alt,
    onLoading = onLoading,
    onFailure = {
        GlideImage(
            resource = alt,
            modifier = modifier,
            contentDescription = contentDescription,
            contentScale = contentScale,
            requestBuilder = requestBuilder
        )
    },
    modifier = modifier,
    contentDescription = contentDescription,
    contentScale = contentScale,
    requestBuilder = requestBuilder.takeIf { resource != null }
)

@Composable
fun GlideImage(
    resource: TargetResource?,
    alt: Painter,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
    onLoading: @Composable () -> Unit = {},
    requestBuilder: RequestBuilder<Drawable>.() -> Unit = {},
) {
    if (resource == null) {
        Image(
            painter = alt,
            modifier = modifier,
            contentDescription = contentDescription,
            contentScale = contentScale,
        )
    } else {
        BaseGlideImage(
            resource = resource,
            onLoading = onLoading,
            onFailure = {
                Image(
                    painter = alt,
                    modifier = modifier,
                    contentDescription = contentDescription,
                    contentScale = contentScale,
                )
            },
            modifier = modifier,
            contentDescription = contentDescription,
            contentScale = contentScale,
            requestBuilder = requestBuilder
        )
    }
}
