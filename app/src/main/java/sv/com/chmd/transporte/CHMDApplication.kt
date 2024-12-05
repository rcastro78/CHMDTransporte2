package sv.com.chmd.transporte

import android.app.Application
import androidx.work.WorkManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import sv.com.chmd.transporte.di.KoinModules

class CHMDApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@CHMDApplication)
            modules(KoinModules.appModule)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        // Cancelar todos los trabajos
        WorkManager.getInstance(applicationContext).cancelAllWork()
    }
}