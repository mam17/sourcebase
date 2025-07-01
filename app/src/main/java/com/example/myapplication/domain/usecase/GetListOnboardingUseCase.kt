package com.example.myapplication.domain.usecase

import com.example.myapplication.R
import com.example.myapplication.domain.layer.OnboardingModel
import javax.inject.Inject

class GetListOnboardingUseCase @Inject constructor() :
    UseCase<GetListOnboardingUseCase.Param, List<OnboardingModel>>() {

    open class Param() : UseCase.Param()

    override suspend fun execute(param: Param): List<OnboardingModel> = listOf(
        OnboardingModel(R.mipmap.ic_launcher, R.string.app_name),
        OnboardingModel(R.mipmap.ic_launcher, R.string.app_name),
        OnboardingModel(R.mipmap.ic_launcher, R.string.app_name),
    )
}