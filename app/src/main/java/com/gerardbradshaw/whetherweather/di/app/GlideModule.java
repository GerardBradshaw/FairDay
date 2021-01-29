package com.gerardbradshaw.whetherweather.di.app;

import android.app.Application;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.gerardbradshaw.whetherweather.R;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class GlideModule {
  
  @Singleton
  @Provides
  static RequestOptions provideGlideRequestOptions() {
    return RequestOptions
        .placeholderOf(R.drawable.img_clear_day)
        .error(R.drawable.img_clear_day);
  }
  
  @Singleton
  @Provides
  static RequestManager provideGlideInstance(Application app, RequestOptions options) {
    return Glide
        .with(app)
        .setDefaultRequestOptions(options);
  }
  
  @Singleton
  @Provides
  static Drawable provideTapToAddPhotoDrawable(Application application) {
    return ContextCompat
        .getDrawable(application, R.drawable.img_clear_day);
  }
}
