package com.example.myapplication.utils.glide

import android.content.Context
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import java.io.InputStream

class AssetModelLoaderFactory(private val context: Context) : ModelLoaderFactory<AssetModel, InputStream> {
    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<AssetModel, InputStream> {
        return AssetModelLoader(context)
    }

    override fun teardown() {}
}
