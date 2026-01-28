package com.example.myapplication.utils.glide
import android.content.Context
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.signature.ObjectKey
import java.io.InputStream

class AssetModelLoader(private val context: Context) : ModelLoader<AssetModel, InputStream> {
    override fun buildLoadData(
        model: AssetModel,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<InputStream>? {
        val key = ObjectKey("asset-${model.assetPath}")
        return ModelLoader.LoadData(key, AssetDataFetcher(context, model.assetPath))
    }

    override fun handles(model: AssetModel): Boolean = true
}
