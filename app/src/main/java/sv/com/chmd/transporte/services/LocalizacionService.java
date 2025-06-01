package sv.com.chmd.transporte.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sv.com.chmd.transporte.R;
import sv.com.chmd.transporte.networking.ITransporte;
import sv.com.chmd.transporte.networking.TransporteAPI;

public class LocalizacionService extends Service {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private SharedPreferences sharedPreferences;
    private ITransporte iTransporte;
    private static final int precision = 60 * 1000; // 60 segundos

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // No es un Bound Service
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences = getSharedPreferences(getString(R.string.spref), MODE_PRIVATE);
        iTransporte = TransporteAPI.Companion.getCHMDService();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        createNotificationChannel();
        startForeground(1, getNotification());

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                Location location = result.getLastLocation();
                if (location != null) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    int altitud = (int) location.getAltitude();
                    int velocidad = (int) (location.getSpeed() * 3.6f); // m/s a km/h

                    Log.i("Geolocalizacion", "Coordenadas: " + lat + ", " + lng + " Velocidad: " + velocidad);

                    // Guardar en SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("latitude", String.valueOf(lat));
                    editor.putString("longitude", String.valueOf(lng));
                    editor.putString("speed", String.valueOf(velocidad));
                    editor.apply();

                    // Enviar al servidor
                    String idRuta = sharedPreferences.getString("idRuta", "");
                    String username = sharedPreferences.getString("username", "");
                    enviarRecorrido(idRuta, username, String.valueOf(lat), String.valueOf(lng), "0", String.valueOf(velocidad));
                }
            }
        };

        startLocationUpdates();
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(precision);
        locationRequest.setFastestInterval(precision);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("Localizacion", "Permisos no concedidos");
            stopSelf();
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void enviarRecorrido(String idRuta, String idAux, String latitud,
                                 String longitud, String emergencia, String velocidad) {
        iTransporte.enviarRuta(idRuta, idAux, latitud, longitud, emergencia, velocidad, "R", "0")
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                            Log.d("RUTA", response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.e("RUTA", "Error enviando recorrido", t);
                    }
                });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Ya se maneja todo en onCreate
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        Log.d("Localizacion", "Servicio detenido");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Seguimiento de ubicación",
                    NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        }
    }

    private Notification getNotification() {
        String CHANNEL_ID = "my_channel_01";
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Localización activada")
                .setContentText("El servicio está recopilando tu ubicación.")
                .setSmallIcon(R.drawable.pin) // Asegúrate de tener un ícono válido
                .build();
    }
}
