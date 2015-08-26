/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.google.openbidder.dataAccessLayer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.openbidder.data.bidding.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tkhalilov
 */
public class DataAccess {

    private final String BaseUrl = "http://imp.mli.me/";

    public List<Creative> GetCreatives() {
        String RequestUrl = "api/data/creatives";
        List<Creative> ReturnValue = new ArrayList<Creative>();

        try {
            URL Url = new URL(BaseUrl + RequestUrl);
            HttpURLConnection con = (HttpURLConnection) Url.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            int ResponseCode = con.getResponseCode();

            if (ResponseCode != -1) {
                BufferedReader In = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuffer response = new StringBuffer();
                String InputLine;

                while ((InputLine = In.readLine()) != null) {
                    response.append(InputLine);
                }

                In.close();

                Gson gson = new GsonBuilder().create();
                Creative[] Creatives = gson.fromJson(response.toString(), Creative[].class);

                for (Creative C : Creatives) {
                    ReturnValue.add(C);
                }
            }

        } catch (Exception EObj) {

        }

        return ReturnValue;
    }

    public List<Rule> GetRules() {

        String RequestUrl = "api/data/rules";
        List<Rule> ReturnValue = new ArrayList<Rule>();

        try {
            URL Url = new URL(BaseUrl + RequestUrl);
            HttpURLConnection con = (HttpURLConnection) Url.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            int ResponseCode = con.getResponseCode();

            if (ResponseCode != -1) {
                
                BufferedReader In = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuffer response = new StringBuffer();
                String InputLine;

                while ((InputLine = In.readLine()) != null) {
                    response.append(InputLine);
                }

                In.close();

                Gson gson = new GsonBuilder().create();
                Rule[] Rules = gson.fromJson(response.toString(), Rule[].class);

                for (Rule R : Rules) {
                    ReturnValue.add(R);
                }
            }

        } catch (Exception EObj) {

        }

        return ReturnValue;
    }

    public List<Config> GetConfigs() {

        String RequestUrl = "api/data/configs";
        List<Config> ReturnValue = new ArrayList<Config>();

        try {
            URL Url = new URL(BaseUrl + RequestUrl);
            HttpURLConnection con = (HttpURLConnection) Url.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            int ResponseCode = con.getResponseCode();

            if (ResponseCode != -1) {
                
                BufferedReader In = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuffer response = new StringBuffer();
                String InputLine;

                while ((InputLine = In.readLine()) != null) {
                    response.append(InputLine);
                }

                In.close();

                Gson gson = new GsonBuilder().create();
                Config[] Configs = gson.fromJson(response.toString(), Config[].class);

                for (Config C : Configs) {
                    ReturnValue.add(C);
                }
            }

        } catch (Exception EObj) {

        }

        return ReturnValue;
    }
}
