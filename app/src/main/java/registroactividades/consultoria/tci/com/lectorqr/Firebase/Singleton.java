package registroactividades.consultoria.tci.com.lectorqr.Firebase;

import android.support.v7.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Singleton extends AppCompatActivity {

    public FirebaseDatabase firebaseDatabase;
    public DatabaseReference databaseReference;

    private static Singleton principal;

    private Singleton() {

    }

    public static Singleton getInstance(){
        if (principal == null ) {
            principal = new Singleton();
        }
        return principal;
    }

    public void InicializarFirebase(){
        if (principal == null ) {
            principal = new Singleton();
        }else{

            FirebaseApp.initializeApp(getInstance());
            firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseDatabase.setPersistenceEnabled(true);

            databaseReference = firebaseDatabase.getReference();
        }
    }

}
