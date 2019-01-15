package registroactividades.consultoria.tci.com.lectorqr;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanBardCore extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    ZXingScannerView scan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scan = new ZXingScannerView(this);
        setContentView(scan);


    }

    @Override
    public void handleResult(Result result) {
        MainActivity.resultT.setText(result.getText());
        onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scan.stopCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();

        scan.setResultHandler(this);
        scan.startCamera();
    }
}
