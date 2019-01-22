package registroactividades.consultoria.tci.com.lectorqr;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import registroactividades.consultoria.tci.com.lectorqr.Firebase.Singleton;
import registroactividades.consultoria.tci.com.lectorqr.Modelo.Ruta;
import registroactividades.consultoria.tci.com.lectorqr.QuickBase.ParseXmlData;
import registroactividades.consultoria.tci.com.lectorqr.QuickBase.Results;

public class MainActivity extends AppCompatActivity {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Date date = new Date();
    private String fecha = dateFormat.format(date);
    private String hora = java.text.DateFormat.getTimeInstance().format(Calendar.getInstance().getTime());
    private String fechaHora = fecha +","+ hora;
    private boolean checkCadena = false;
    public static String codigo = "";
    public static String codigoSplit [];
    private ImageButton scan, upQuick;
    private static final String[] PERMISOS = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
    };
    private static final int REQUEST_CODE = 1;
    private Singleton s = Singleton.getInstance();
    private LottieAnimationView check;
    private Ruta r = new Ruta();
    private EditText descripcion, importe;
    private Spinner spnConcepto;
    private double lati = 0;
    private double longi  = 0;
    private LocationManager manager;
    private AlertDialog alert = null;
    private ArrayList<Ruta> datosR;
    private String token = "dv6unakbccxz34c8d3kp6pb56w6";
    private TelephonyManager mTelephony;
    private String myIMEI = "";
    public static boolean connected;
    private CardView cardView;
    private TextView pendiente;
    private SwipeRefreshLayout refreshLayout;
    private ArrayList<String> UID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
     if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

        ActivityCompat.requestPermissions(MainActivity.this, PERMISOS , REQUEST_CODE);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Init();

        upQuick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validaInternet();
                if(connected){
                    if(datosR.size() > 0){
                        for(int i=0; i<datosR.size(); i++){
                            subirQuick(i);
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "Recargar", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "No tienes internet, verifica tu conexión", Toast.LENGTH_LONG).show();
                }
            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadDatos();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });

        try{
            s.InicializarFirebase();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "Error en instancia", Toast.LENGTH_LONG).show();
        }

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISOS , REQUEST_CODE);
                }else{
                    startActivity(new Intent(MainActivity.this, ScanBardCore.class));
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getIMEI();
                    validaInternet();
                    Mi_hubicacion();
                } else {
                    for(int i =0; i<permissions.length; i++){
                        if(grantResults[i] < 0){
                            Toast.makeText(getApplicationContext(), "Estos permisos "+permissions[i]+" son necesarios.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        }
    }

    private void Init() {
        check = findViewById(R.id.animation_view);
        scan = findViewById(R.id.button);
        descripcion = findViewById(R.id.txtDexcripcion);
        importe = findViewById(R.id.txtImporte);
        spnConcepto = findViewById(R.id.spnConcept);
        datosR = new ArrayList<>();
        cardView = findViewById(R.id.cardSubir);
        pendiente = findViewById(R.id.txtPendiente);
        refreshLayout = findViewById(R.id.swipeLoad);
        upQuick = findViewById(R.id.imageButton);
        UID = new ArrayList<>();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
                case R.id.save:
                    if(!descripcion.getText().toString().isEmpty()){
                        if(!importe.getText().toString().isEmpty()){
                            if(spnConcepto.getSelectedItemPosition() > 0){
                                if(checkCadena){
                                    if(connected){
                                        saveData();
                                        if(datosR.size() > 0){
                                            for(int i=0; i<datosR.size(); i++){
                                                subirQuick(i);
                                            }
                                        }else{
                                            Toast.makeText(getApplicationContext(), "Recargar", Toast.LENGTH_LONG).show();
                                        }
                                    }else{
                                        Toast.makeText(getApplicationContext(), "No tienes internet, pero tus datos se han guardado correctamente", Toast.LENGTH_LONG).show();
                                        saveData2();

                                    }
                                }else {
                                    Toast.makeText(getApplicationContext(), "El codigo del cliente no es correcto", Toast.LENGTH_LONG).show();
                                }
                            }else{
                                Toast.makeText(getApplicationContext(), "Selecciones una opcion por favor", Toast.LENGTH_LONG).show();
                            }
                        }else{
                            importe.setError("Es requerido");
                        }
                    }else{
                        descripcion.setError("Es requerido");
                    }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if(!codigo.isEmpty()){
                        check.setEnabled(true);
                        check.playAnimation();
                        checkCadena = true;
                    }
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        },1000);
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            actualizar(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void Mi_hubicacion() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions(MainActivity.this, PERMISOS , REQUEST_CODE);
        }else{
            try{
                manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                    AlertNoGps();
                }else{
                    Location local = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    actualizar(local);

                    manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20 * 1000, 10, locationListener);
                }
            }catch (Exception e){
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void AlertNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder .setTitle("GPS")
                .setMessage("El sistema GPS esta desactivado, para registrar tus actividades es necesario activarlo." +
                        " Por favor pulsa el botón rojo (Activar) para activarlo.")
                .setCancelable(false)
                .setPositiveButton("Activar", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                    }
                });
        alert = builder.create();
        alert.show();
        alert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.RED);
    }

    private void actualizar(Location local) {
        if (local != null) {
            lati = local.getLatitude();
            longi = local.getLongitude();
        }
    }

    public void saveData(){
        r.setConcepto(spnConcepto.getSelectedItem().toString());
        r.setImporte(Double.valueOf(importe.getText().toString()));
        r.setDescripcion(descripcion.getText().toString());
        r.setFechaHora(fechaHora);
        r.setLatitud(lati);
        r.setLongitud(longi);
        r.setIMEI(myIMEI);
        r.setStatus(1);

        s.databaseReference
        .child("Actividades/"+getIMEI()+"/"+UUID.randomUUID().toString())
        .setValue(r);

        datosR.clear();
        datosR.add(r);
    }

    public void saveData2(){
        r.setConcepto(spnConcepto.getSelectedItem().toString());
        r.setImporte(Double.valueOf(importe.getText().toString()));
        r.setDescripcion(descripcion.getText().toString());
        r.setFechaHora(fechaHora);
        r.setLatitud(lati);
        r.setLongitud(longi);
        r.setIMEI(myIMEI);
        r.setStatus(0);

        s.databaseReference
        .child("Actividades/"+getIMEI()+"/"+UUID.randomUUID().toString())
        .setValue(r);

        limpiarDatos();
    }

    public void saveData3(int pos){
        r.setConcepto(datosR.get(pos).getConcepto());
        r.setImporte(datosR.get(pos).getImporte());
        r.setDescripcion(datosR.get(pos).getDescripcion());
        r.setFechaHora(datosR.get(pos).getFechaHora());
        r.setLatitud(datosR.get(pos).getLatitud());
        r.setLongitud(datosR.get(pos).getLongitud());
        r.setIMEI(datosR.get(pos).getIMEI());
        r.setStatus(1);

        s.databaseReference
        .child("Actividades/"+getIMEI()+"/"+UID.get(pos))
        .setValue(r);
    }

    public void subirQuick(int pos){
        String  RegistroQ = "";
        RegistroQ = "https://aortizdemontellanoarevalo.quickbase.com/db/bnu3vjsyt" +
                "?a=API_AddRecord"+
                "&_fid_6="+datosR.get(pos).getImporte()+ //Importe
                "&_fid_10="+datosR.get(pos).getConcepto()+ //Concepto
                "&_fid_12="+datosR.get(pos).getLatitud() +", "+ datosR.get(pos).getLongitud()+ //Ubicacion-Lat-Long
                "&_fid_18="+datosR.get(pos).getIMEI()+ //Related User App
                "&_fid_19="+datosR.get(pos).getFechaHora()+ //Fecha
                "&ticket="  +"9_bpqnx8hh8_b2c6pu_fwjc_a_-b_di9hv2qb4t5jbp9jhvu3thpdfdt49mr8dugqz499kgcecg5vb3m_bwg8928"+
                "&apptoken=" + token;
        try{
            new CargarDatos().execute(RegistroQ.replace(" ", "%20"));
        } catch (Exception e){
            Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show();
        }
    }

    class CargarDatos extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                while (true) {
                    return Results.downloadUrl(urls[0]);
                }

            } catch (IOException e) {
                cancel(true);
                return e.getCause().toString();
            }
        }

        @Override
        protected void onPostExecute(String result) {

            String resultado = ParseXmlData.ParseXmlData(result);

            if (resultado != null) {
                if (resultado.equals("No error")) {
                    Log.d("Mensaje del Servidor", resultado);
                    try {
                        Toast.makeText(getApplicationContext(), "Se subio la informacion correctamente", Toast.LENGTH_LONG).show();
                        limpiarDatos();
                        for(int i = 0; i<UID.size(); i++){
                            saveData3(i);
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "error al subir: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d("Error de consulta", resultado);
                    Toast.makeText(getApplicationContext(), "Error de consulta"+ resultado, Toast.LENGTH_LONG).show();
                }
            } else {
                Log.d("Error del Servidor ", result);
                Toast.makeText(getApplicationContext(), "Error del Servidor " + result, Toast.LENGTH_LONG).show();
            }
        }
    }

    public String getIMEI(){
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, PERMISOS , REQUEST_CODE);
        }else{
            mTelephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (mTelephony.getDeviceId() != null){
                myIMEI = mTelephony.getDeviceId();
            }
        }
        return myIMEI;
    }

    public void loadDatos(){
        datosR.clear();
        try{
            r = new Ruta();
            s.databaseReference.child("Actividades/"+getIMEI())
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot objSnaptshot : dataSnapshot.getChildren()){
                            r = objSnaptshot.getValue(Ruta.class);
                            if(r.getStatus() < 1){
                                UID.add(objSnaptshot.getKey());
                                datosR.add(r);
                                cardView.setVisibility(View.VISIBLE);
                                pendiente.setText(datosR.size()+"");
                            }
                        }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }catch (Exception e){

        }
    }

    public void validaInternet(){
        DatabaseReference connectedRef = s.firebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                connected = snapshot.getValue(Boolean.class);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Error internet:" + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void limpiarDatos(){
        descripcion.setText("");
        importe.setText("");
        spnConcepto.setSelection(0);
        codigo = "";
        check.setEnabled(false);
        cardView.setVisibility(View.INVISIBLE);
    }
}
