package com.ner.landslide.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.ner.landslide.BuildConfig
import com.ner.landslide.data.local.database.NERDatabase
import com.ner.landslide.data.remote.api.PredictionApi
import com.ner.landslide.data.remote.api.WeatherApi
import com.ner.landslide.data.remote.firestore.*
import com.ner.landslide.data.repository.*
import com.ner.landslide.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @Named("prediction")
    fun providePredictionRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.PREDICTION_API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @Named("weather")
    fun provideWeatherRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.WEATHER_API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun providePredictionApi(@Named("prediction") retrofit: Retrofit): PredictionApi =
        retrofit.create(PredictionApi::class.java)

    @Provides
    @Singleton
    fun provideWeatherApi(@Named("weather") retrofit: Retrofit): WeatherApi =
        retrofit.create(WeatherApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNERDatabase(@ApplicationContext context: Context): NERDatabase =
        Room.databaseBuilder(context, NERDatabase::class.java, "ner_database")
            .fallbackToDestructiveMigration()
            .build()
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
object FirestoreSourceModule {

    @Provides @Singleton
    fun provideAlertFirestoreSource(fs: FirebaseFirestore) = AlertFirestoreSource(fs)

    @Provides @Singleton
    fun provideReportFirestoreSource(fs: FirebaseFirestore) = ReportFirestoreSource(fs)

    @Provides @Singleton
    fun provideSOSFirestoreSource(fs: FirebaseFirestore) = SOSFirestoreSource(fs)

    @Provides @Singleton
    fun provideRiskZoneFirestoreSource(fs: FirebaseFirestore) = RiskZoneFirestoreSource(fs)
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides @Singleton
    fun provideAlertRepository(source: AlertFirestoreSource): AlertRepository =
        AlertRepositoryImpl(source)

    @Provides @Singleton
    fun provideReportRepository(
        source: ReportFirestoreSource,
        db: NERDatabase
    ): ReportRepository = ReportRepositoryImpl(source, db)

    @Provides @Singleton
    fun provideRiskZoneRepository(source: RiskZoneFirestoreSource): RiskZoneRepository =
        RiskZoneRepositoryImpl(source)

    @Provides @Singleton
    fun providePredictionRepository(api: PredictionApi): PredictionRepository =
        PredictionRepositoryImpl(api)

    @Provides @Singleton
    fun provideWeatherRepository(api: WeatherApi): WeatherRepository =
        WeatherRepositoryImpl(api)

    @Provides @Singleton
    fun provideSOSRepository(source: SOSFirestoreSource): SOSRepository =
        SOSRepositoryImpl(source)

    @Provides @Singleton
    fun provideUserRepository(
        @ApplicationContext context: Context,
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): UserRepository = UserRepositoryImpl(context, auth, firestore)
}
