package com.example.myapplication.utils.glide

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import java.io.File

object ImageUtils {

    fun ImageView.loadFromPathAction(
        path: String,
        radius: Int = 0,
        onLoaded: (() -> Unit)? = null
    ) {
        val context = this.context

        val isAsset = !path.startsWith("http://")
                && !path.startsWith("https://")
                && !path.startsWith("content://")
                && !path.startsWith("file://")
                && !File(path).exists()

        val glideModel: Any = if (isAsset) {
            AssetModel(path)
        } else {
            when {
                path.startsWith("content://") -> path.toUri()
                path.startsWith("file://") -> path
                path.startsWith("http") -> path
                File(path).exists() -> File(path)
                else -> path
            }
        }

        val transformation = if (radius > 0) {
            MultiTransformation(CenterCrop(), RoundedCorners(radius))
        } else CenterCrop()

        val shimmerDrawable = createShimmerPlaceholder()

        Glide.with(context)
            .load(glideModel)
            .placeholder(shimmerDrawable)
            .apply(RequestOptions().transform(transformation))
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?, model: Any?, target: Target<Drawable?>, isFirstResource: Boolean
                ): Boolean {
                    shimmerDrawable.stopShimmer()
                    onLoaded?.invoke()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable, model: Any, target: Target<Drawable?>?, dataSource: DataSource, isFirstResource: Boolean
                ): Boolean {
                    shimmerDrawable.stopShimmer()
                    onLoaded?.invoke()
                    return false
                }
            })
            .into(this)
    }

    fun createShimmerPlaceholder(): ShimmerDrawable {
        val shimmer = Shimmer.AlphaHighlightBuilder()
            .setDuration(1000)
            .setBaseAlpha(0.7f)
            .setHighlightAlpha(0.4f)
            .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
            .setAutoStart(true)
            .build()

        val shimmerDrawable = ShimmerDrawable().apply {
            setShimmer(shimmer)
        }
        return shimmerDrawable
    }
}