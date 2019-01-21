package registroactividades.consultoria.tci.com.lectorqr.QuickBase;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class ParseXmlData {

private static String text;
    public static String ParseXmlData(String xmlData){



        XmlPullParserFactory pullParserFactory;

        try {

            pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();

            StringReader stringReader = new StringReader(xmlData);

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,false);
            parser.setInput(stringReader);

            ArrayList<Response_QB> respuestaQb = parseXml(parser);


            for (Response_QB responseQB:respuestaQb){
                text = responseQB.getErrText();

            }


        } catch (XmlPullParserException e) {

            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }


        return text;

    }


    private static ArrayList<Response_QB> parseXml(XmlPullParser parser)throws XmlPullParserException,IOException {

        ArrayList<Response_QB> arrayResponseQb = null;
        int eventType = parser.getEventType();
        Response_QB responseQB =  null;

        while (eventType != XmlPullParser.END_DOCUMENT){
            String atributo;

            switch (eventType){

                case XmlPullParser.START_DOCUMENT:
                    arrayResponseQb = new ArrayList<>();
                    break;

                case XmlPullParser.START_TAG:
                    atributo = parser.getName();

                    if (atributo.equals("action")){
                        responseQB = new Response_QB();
                        responseQB.action = parser.nextText();
                    }else if(responseQB !=null){
                        if (atributo.equals("errcode")){
                            responseQB.errCode = parser.nextText();
                        }else if(atributo.equals("errtext")){
                            responseQB.errText = parser.nextText();

                        }else if(atributo.equals("rid")){
                            responseQB.rid = parser.nextText();

                        }else if(atributo.equals("update_id")){
                            responseQB.update_id = parser.nextText();

                        }
                    }
                    break;


                case XmlPullParser.END_TAG:
                    atributo = parser.getName();
                    if (atributo.equalsIgnoreCase("qdbapi") && responseQB != null){
                        arrayResponseQb.add(responseQB);

                    }
            }
            eventType = parser.next();
        }
        return arrayResponseQb;

    }


}
