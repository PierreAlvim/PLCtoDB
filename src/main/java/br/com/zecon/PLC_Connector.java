package br.com.zecon;

import com.google.gson.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class PLC_Connector<T> {

    private String URL;
    private GsonBuilder gb;
    private Gson gs;
    private Class<T> type;


    public PLC_Connector(String URL, Class<T> type){
        this.URL = URL;
        this.type = type;

        //Gson SETUP
        gb = new GsonBuilder();
        gs = gb.registerTypeAdapter(Timestamp.class, new PLC_DateDeserializer()).create();
    }

    private String getPLCData() throws IOException {
        String line = "", all = "";
        java.net.URL url = null;
        BufferedReader in = null;
        try {
            url = new URL(URL);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setReadTimeout(1000);
            in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            while ((line = in.readLine()) != null) {
                all += line;
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }

        return all;
    }

    public T getJsonObj() throws IOException, JsonSyntaxException
    {
        return gs.fromJson(getPLCData(), type);
    }

    private class PLC_DateDeserializer implements JsonDeserializer<Timestamp> {
        public Timestamp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            String ds = "";
            try {
                //fast formatter
                //"DTL#2019-10-21-13:24:18.908596" -> "2019-10-21-13:24:18"
                ds = json.getAsJsonPrimitive().getAsString().split("#")[1].split(".")[0];

                System.out.println("Convertido com sucesso");

                return Timestamp.valueOf(ds);
            }
            catch (Exception e)
            {
                return Timestamp.valueOf(LocalDateTime.now());
            }


        }
    }

}
