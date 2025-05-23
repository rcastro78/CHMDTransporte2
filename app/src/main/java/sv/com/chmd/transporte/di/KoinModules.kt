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
import sv.com.chmd.transporte.repository.AsistenciaManRepository
import sv.com.chmd.transporte.repository.AsistenciaTarRepository
import sv.com.chmd.transporte.repository.LoginRepository
import sv.com.chmd.transporte.repository.RutaRepository
import sv.com.chmd.transporte.services.NetworkChangeReceiver
import sv.com.chmd.transporte.viewmodel.AsistenciaManViewModel
import sv.com.chmd.transporte.viewmodel.AsistenciaTarViewModel
import sv.com.chmd.transporte.viewmodel.CierreRutaViewModel
import sv.com.chmd.transporte.viewmodel.LoginViewModel
import sv.com.chmd.transporte.viewmodel.SeleccionRutaViewModel
import sv.com.chmd.transporte.viewmodel.TransporteViewModel
import sv.com.chmd.transporte.viewmodel.ValidarDispositivoViewModel

object KoinModules {
    val appModule = module {
        single<SharedPreferences> {
            androidContext().getSharedPreferences(androidContext().getString(R.string.spref), Context.MODE_PRIVATE)
        }
        single<ITransporte> {
            TransporteAPI.getCHMDService()!! }

        single { TransporteDB.getInstance(androidContext()) }
        single { RutaRepository(get(), get(), androidContext()) }

        single { NetworkChangeReceiver() }
        viewModel { LoginViewModel(get(),get()) }
        viewModel { AsistenciaManViewModel(get(),get()) }
        viewModel { AsistenciaTarViewModel(get(),get()) }
        viewModel { ValidarDispositivoViewModel(get(),get()) }
        viewModel { CierreRutaViewModel(get(),get()) }
        viewModel { SeleccionRutaViewModel(get()) }
        viewModel { TransporteViewModel()}

        single { AsistenciaManRepository(get()) }
        single { AsistenciaTarRepository(get()) }
        single {LoginRepository(get())}
    }

}