/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.zecon;

import br.com.zecon.log.LogEvent;
import br.com.zecon.log.LogEventError;
import br.com.zecon.log.LogEventInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static br.com.zecon.log.LogEvent.EventTypes.ERROR;
import static br.com.zecon.log.LogEvent.EventTypes.OK;
//import sun.management.snmp.jvminstr.JvmThreadInstanceEntryImpl;

/**
 * @author Pierre Alvim de Paula
 */

public class TesteDB {

    /**
     * --- File path for data backup when offline ---
     */
    private static final String FILE_PATH_OLD_DATA = "sensor_backup.data";
    /**
     * --- File path for logging every event occurred like errors, warning... ---
     */
    private static final String FILE_PATH_ERROR_LOG = "events.log";
    /**
     * --- PLC URL (Local IP) ---
     */
    private static final String URL_PLC = "http://192.168.1.51/awp/MonitorUsina/io_json";

    private static GsonBuilder gb;
    private static Gson gs;

    private static PLC_Connector<SensoresTeste> plc_con;

    private static EntityManagerFactory factory;
    private static SensoresTesteJpaController con;

    private static ArrayList<SensoresTeste> data_list;

    public static void main(String[] args) {


        /* Hibernate SETUP */
        factory = Persistence.createEntityManagerFactory("UsinaPU");
        con = new SensoresTesteJpaController(factory);

        /* Gson SETUP */
        gb = new GsonBuilder();
        gs = gb.create();

        plc_con = new PLC_Connector<>(URL_PLC + "/testeSensores.json", SensoresTeste.class);

        data_list = new ArrayList<>();

        try {

            String content = new String(Files.readAllBytes(Paths.get(FILE_PATH_OLD_DATA)));
            data_list = gs.fromJson(content, data_list.getClass());
            logInfo(OK, "Old backup Loaded: " + data_list);

        } catch (IOException e) {
            logError( e, "Error reading old data backup file.");
            //e.printStackTrace();
        }


        /* Thread SETUP */
        Timer time = new Timer(); // Instantiate Timer Object
        PeriodicChecker st = new PeriodicChecker(); // Instantiate SheduledTask class
        //time.schedule(st, 0, 10000); // Create Repetitively task for every 2 secs
    }

    static public class PeriodicChecker extends TimerTask {
        @Override
        public void run() {

            System.out.println("Iniciando envio:");

            /* --- Busca Json no PLC --- */
            SensoresTeste s = null;
            try {
                s = plc_con.getJsonObj();
            } catch (IOException e) {
                logError(e,"Local connection error, can't reach PLC!");
            } catch (JsonSyntaxException je) {
                logError(je, "Error on the Json format.");
            }
            /* ------------------------------------- */

            /* Send old data if founded --- */
            try {
                while (!data_list.isEmpty()) {
                    sendDataSensor(data_list.get(0));           //Send each obj on the list
                    data_list.remove(0);                    //Remove sent obj from the list
                    writeDataFile(data_list);                   //Update backup file
                }
                logInfo(OK, "Old Data sent!");
            } catch (Exception ex) {
                logError(ex, "Error persisting old data!");
            }
            /* ------------------------------------- */

            /* --- Send to the DB, the JSON object received from the PLC --- */
            try {
                sendDataSensor(s);
            } catch (Exception ex)                              //Save data to send later and backup on file
            {
                logError(ex, "Error on persistence!");

                data_list.add(s);
                writeDataFile(data_list);
            }
            /* ------------------------------------- */
        }
    }


    /**
     * Write on log file using info from the LogEventError, the file path is define by {@link #FILE_PATH_ERROR_LOG}
     *
     * @param event Event data structure with all info needed.
     */
    private static void writeLogFile(LogEvent event) {
        try {
            Files.write(Paths.get(FILE_PATH_ERROR_LOG), event.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Log an error and an exception cause into the log file.
     *
     * @param e   Exception that fired the error.
     * @param obs Observation or explanation of the event.
     * @see #writeLogFile
     * @see #FILE_PATH_ERROR_LOG
     */
    private static void logError(Exception e, String obs) {
        writeLogFile(new LogEventError(e, obs));
        System.out.println(ERROR.text + " :\n" + obs + " :\n" + e);
    }

    /**
     * Log an info event into log file.
     *
     * @param type Type of the event : {@link LogEvent.EventTypes}.
     * @param obs  Observation or explanation of the event.
     * @see #writeLogFile
     * @see #FILE_PATH_ERROR_LOG
     */
    private static void logInfo(LogEvent.EventTypes type, String obs) {
        writeLogFile(new LogEventInfo(type, obs));
        System.out.println(type.text + " :\n" + obs);
    }


    /**
     * Write on file, all objects of the passed list, overwriting any previous data. The file path is define by {@link #FILE_PATH_OLD_DATA}
     *
     * @param list List os objects to save using Json serialization.
     */
    public static void writeDataFile(List<SensoresTeste> list) {
        try {
            Files.write(Paths.get(FILE_PATH_OLD_DATA), gs.toJson(list, list.getClass()).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Persist data on DB.
     * @param s sensor data.
     * @throws Exception
     */
    public static void sendDataSensor(SensoresTeste s) throws Exception {
        con.create(s);
    }

    public static boolean testDBConnection() throws IOException {

        //---- Getting DB url from hibernate properties
        String adr = (String) (factory.getProperties().get("hibernate.connection.url"));

        //---- Formats the string to a simple url
        adr = adr.split("://")[1].split(":")[0];

        InetAddress inetAddress = InetAddress.getByName(adr);

        return inetAddress.isReachable(1000);

    }


}
