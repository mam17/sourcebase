package com.example.myapplication.domain.usecase

import com.example.myapplication.R
import com.example.myapplication.domain.layer.LanguageModel
import javax.inject.Inject

class GetListLanguageUseCase @Inject constructor() :
    UseCase<GetListLanguageUseCase.Param, List<LanguageModel>>() {

    open class Param() : UseCase.Param()

    override suspend fun execute(param: Param): List<LanguageModel> = listOf(
        LanguageModel("en", R.drawable.ic_lang_english, R.string.txt_english),
        LanguageModel("vi", R.drawable.ic_lang_vietnamese, R.string.txt_vietnamese),
        LanguageModel("hi", R.drawable.ic_lang_hindi, R.string.txt_hindi),
        LanguageModel("es", R.drawable.ic_lang_spanish, R.string.txt_spanish),
        LanguageModel("fr", R.drawable.ic_lang_france, R.string.txt_french),
        LanguageModel("pt", R.drawable.ic_lang_portugal, R.string.txt_portuguese)
    )
}