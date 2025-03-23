package sv.com.chmd.transporte.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import sv.com.chmd.transporte.R
import sv.com.chmd.transporte.db.TransporteDB
import sv.com.chmd.transporte.networking.ITransporte
import sv.com.chmd.transporte.networking.TransporteAPI
import sv.com.chmd.transporte.services.NetworkChangeReceiver
import sv.com.chmd.transporte.viewmodel.AsistenciaManViewModel
import sv.com.chmd.transporte.viewmodel.AsistenciaTarViewModel
import sv.com.chmd.transporte.viewmodel.CierreRutaViewModel
import sv.com.chmd.transporte.viewmodel.LoginViewModel
import sv.com.chmd.transporte.viewmodel.SeleccionRutaViewModel
import sv.com.chmd.transporte.viewmodel.ValidarDispositivoViewModel

object KoinModules {
    val appModule = module {
        single<SharedPreferences> {
            androidContext().getSharedPreferences(androidContext().getString(R.string.spref), Context.MODE_PRIVATE)
        }
        /*single<ITransporte> {
            (url:String?)->
            TransporteAPI.getCHMDService(url!!)!! }*/
        single<ITransporte> {
            TransporteAPI.getCHMDService()!! }


        single { NetworkChangeReceiver() }
        viewModel { LoginViewModel(get(),get()) }
        viewModel { AsistenciaManViewModel(get(),get()) }
        viewModel { AsistenciaTarViewModel(get(),get()) }
        viewModel { ValidarDispositivoViewModel(get(),get()) }
        viewModel { CierreRutaViewModel(get(),get()) }

    }


    fun provideDatabase(context: Context): TransporteDB {
        return Room.databaseBuilder(
            context.applicationContext,
            TransporteDB::class.java,
            "transporte_chmd_000.db"
        ).build()
    }

}