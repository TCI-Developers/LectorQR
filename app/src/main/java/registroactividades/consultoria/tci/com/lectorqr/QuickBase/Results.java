package registroactividades.consultoria.tci.com.lectorqr.QuickBase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Results {


    public static String downloadUrl(String myurl) throws IOException {
        myurl = myurl.replace(" ", "%20");
        InputStream is = null;
        // Othnly display e first 500 characters of the retrieved
        // web page content.
        int len = 1999999999;
        System.out.println(myurl);
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(100000 /* milliseconds */);
            conn.setConnectTimeout(100000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(myurl); //Url encoded parameters
            writer.flush();
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();

            //validamos la respuesta del servidor, si es 200 continuamos con el proceso,
            // de lo contrario nos retornara la respuesta de error del servidor "404"
            if (response == 200){
                System.out.println("Respuesta " + "The response is: " + response);
                is = conn.getInputStream();
                // Convert the InputStream into a string
                String contentAsString = readIt(is, len);
                // String contentAsString = ParseXmlData(is,len);
                return contentAsString;

            }else{
                //retorna el codigo del error generado
                return  String.valueOf(response);
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public static String readIt(InputStream stream, int len) throws IOException {
        InputStreamReader reader = new InputStreamReader(stream,"UTF-8");
        BufferedReader br = new BufferedReader(reader);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null){
            sb.append(line+"\n");
        }
        return sb.toString();
    }

}
