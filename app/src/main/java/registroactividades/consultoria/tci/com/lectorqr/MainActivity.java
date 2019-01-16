package registroactividades.consultoria.tci.com.lectorqr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import registroactividades.consultoria.tci.com.lectorqr.Firebase.Singleton;
import registroactividades.consultoria.tci.com.lectorqr.Modelo.Ruta;

public class MainActivity extends AppCompatActivity {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Date date = new Date();
    private String fecha = dateFormat.format(date);
    private String hora = java.text.DateFormat.getTimeInstance().format(Calendar.getInstance().getTime());
    private String fechaHora = fecha + hora;

    public static String codigo = "";
    public static String codigoSplit [];
    private ImageButton scan;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int leer = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        int leer2 = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int leer3 = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        int leer4 = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE);
        if (leer == PackageManager.PERMISSION_DENIED || leer2 == PackageManager.PERMISSION_DENIED || leer3 == PackageManager.PERMISSION_DENIED || leer4 == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, PERMISOS, REQUEST_CODE);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        check = findViewById(R.id.animation_view);
        scan = findViewById(R.id.button);
        descripcion = findViewById(R.id.txtDexcripcion);
        importe = findViewById(R.id.txtImporte);
        spnConcepto = findViewById(R.id.spnConcept);

        try{
            s.InicializarFirebase();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "Error en instancia", Toast.LENGTH_LONG).show();
        }

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ScanBardCore.class));
            }
        });
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
                    saveData();
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
                        check.playAnimation();
                    }
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        },1000);
    }

    public void saveData(){
        r.setConcepto(spnConcepto.getSelectedItem().toString());
        r.setImporte(Double.valueOf(importe.getText().toString()));
        r.setDescripcion(descripcion.getText().toString());
        


        s.databaseReference
                .child("Actividades/"+UUID.randomUUID().toString())
                .setValue();
    }
}
