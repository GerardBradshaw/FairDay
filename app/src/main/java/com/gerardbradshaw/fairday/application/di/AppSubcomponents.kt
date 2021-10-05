package com.gerardbradshaw.fairday.application.di

import com.gerardbradshaw.fairday.activities.detail.DetailActivityComponent
import com.gerardbradshaw.fairday.activities.saved.SavedActivityComponent
import dagger.Module

@Module(subcomponents = [DetailActivityComponent::class, SavedActivityComponent::class])
interface AppSubcomponents