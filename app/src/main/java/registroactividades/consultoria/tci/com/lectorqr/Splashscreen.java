package registroactividades.consultoria.tci.com.lectorqr;


import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import registroactividades.consultoria.tci.com.lectorqr.Firebase.Singleton;

public class Splashscreen extends AppCompatActivity {

    ImageView imgv_logo;
    TextView textView1,textView2,textView3,textView4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        fullscreen();
        init();
        animar();
        try{
        }catch (Exception e){

        }
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    startActivity(new Intent(Splashscreen.this, MainActivity.class));
                    finish();
                }
            }
        }; thread.start();
    }

    public void init(){
        imgv_logo = findViewById(R.id.imgv_logo);
        textView1 = findViewById(R.id.text1);
        textView2 = findViewById(R.id.text2);
        textView3 = findViewById(R.id.text3);
        textView4 = findViewById(R.id.text4);
    }

    public void animar(){
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.splash_transition);
        imgv_logo.startAnimation(animation);
        textView1.startAnimation(animation);
        textView2.startAnimation(animation);
        textView3.startAnimation(animation);
        textView4.startAnimation(animation);
    }

    private void fullscreen(){
        int newUI = getWindow().getDecorView().getSystemUiVisibility();
        if(Build.VERSION.SDK_INT >= 14){newUI ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;}
        if(Build.VERSION.SDK_INT >= 16){newUI ^= View.SYSTEM_UI_FLAG_FULLSCREEN;}
        if(Build.VERSION.SDK_INT >= 18){newUI ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;}
        getWindow().getDecorView().setSystemUiVisibility(newUI);
    }
}
