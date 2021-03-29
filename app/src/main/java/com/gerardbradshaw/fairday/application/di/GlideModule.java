package com.gerardbradshaw.fairday.application.di;

import android.app.Application;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.gerardbradshaw.fairday.R;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class GlideModule {
  
  @Singleton
  @Provides
  static RequestOptions provideGlideRequestOptions() {
    return new RequestOptions().error(R.drawable.img_day_clear);
  }
  
  @Singleton
  @Provides
  static RequestManager provideGlideInstance(Application app, RequestOptions options) {
    return Glide
        .with(app)
        .setDefaultRequestOptions(options);
  }
}
