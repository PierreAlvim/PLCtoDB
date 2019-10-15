/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.zecon;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;
//import sun.management.snmp.jvminstr.JvmThreadInstanceEntryImpl;

/**
 * @author Pierre Alvim de Paula
 */
public class TesteDB {

    private static final String FILE_PATH_OLD_DATA = "sensor_backup.data";
    private static final String FILE_PATH_ERROR_LOG = "events.log";
    private static GsonBuilder gb;
    private static Gson gs;

    private static EntityManagerFactory factory;
    private static SensoresTesteJpaController con;

    private static ArrayList<SensoresTeste> data_list;

    public static void main(String[] args) {

        //Hibernate SETUP
        factory = Persistence.createEntityManagerFactory("UsinaPU");
        con = new SensoresTesteJpaController(factory);
        //factory.close();

        //Gson SETUP
        gb = new GsonBuilder();
        gs = gb.setDateFormat("yyyy-MM-dd HH:mm:ss").create();

        data_list = new ArrayList<>();
        //gs.fromJson(gs.toJson(data_list), data_list.getClass());
        //System.out.println(gs.fromJson(gs.toJson(data_list), data_list.getClass()));
        writeDataFile(data_list);
        try {
            String content = new String(Files.readAllBytes(Paths.get(FILE_PATH_OLD_DATA)));
            data_list = gs.fromJson(content, data_list.getClass());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Old data: " + data_list);

        //Thread SETUP
        Timer time = new Timer(); // Instantiate Timer Object
        PeriodicChecker st = new PeriodicChecker(); // Instantiate SheduledTask class
        time.schedule(st, 0, 10000); // Create Repetitively task for every 2 secs
    }

    static public class PeriodicChecker extends TimerTask {
        @Override
        public void run() {

            System.out.println("Iniciando envio:");
            //gerando objeto aleatorio
            SensoresTeste s = new SensoresTeste(null,
                    new Timestamp(new Date().getTime()),
                    1200f, 23f, 2f,
                    false, true, false, true);
            try {
                //teste Entidade
//                SensoresTeste s = new SensoresTeste(0,
//                        Timestamp.valueOf("2019-10-07 90:56:00"),
//                        0f,0f,0f,
//                        0,0,0,0);
                while (!data_list.isEmpty()) {
                    sendDataSensor(data_list.get(0));
                    data_list.remove(0);
                    writeDataFile(data_list);
                }
            } catch (Exception ex) {
                System.out.println("Error sendind old data!");
            }

            try {
                sendDataSensor(s);
            } catch (Exception ex) {
                System.out.println("CONNECTION ERROR! " + ex);
                data_list.add(s);
                writeDataFile(data_list);
            }
        }
    }


    public static void writeDataFile(List<SensoresTeste> list) {
        try {
            Files.write(Paths.get("sensor_backup.data"), gs.toJson(list, list.getClass()).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendDataSensor(SensoresTeste s) throws Exception {

        //System.out.println(factory.getProperties().get("hibernate.connection.url"));
        //InetAddress inetAddress = InetAddress.getByName(factory.getProperties());
        con.create(s);

    }
    public static boolean testDBConnection() throws IOException{

        //---- Getting DB url from hibernate properties
        String adr = (String)(factory.getProperties().get("hibernate.connection.url"));

        //---- Formats the string to a simple url
        adr = adr.split("://")[1].split(":")[0];

        InetAddress inetAddress = InetAddress.getByName(adr);

        return inetAddress.isReachable(1000);

    }


}
