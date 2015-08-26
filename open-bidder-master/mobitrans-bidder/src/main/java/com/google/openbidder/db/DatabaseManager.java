/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.google.openbidder.db;

import com.google.openbidder.data.bidding.*;
import java.sql.*;
import java.util.*;

/**
 *
 * @author tkhalilov
 */
public class DatabaseManager {

    private final String ConnectionUrl = "jdbc:jtds:sqlserver://172.30.0.165;databaseName=RTB;user=tarek;password=koller60";

    public DatabaseManager() {

    }

    public List<Creative> FetchCreatives() {
        List<Creative> Creatives = new ArrayList<Creative>();
        Connection DBConnection = null;
        Statement DBStatement = null;

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");

            DBConnection = DriverManager.getConnection(ConnectionUrl);
            DBStatement = DBConnection.createStatement();

            CallableStatement CStatement = DBConnection.prepareCall("{call Get_BiddingCreatives}");
            ResultSet Rs = CStatement.executeQuery();

            while (Rs.next()) {

                Creative Cr = new Creative();
                Cr.ID = Rs.getInt("ID");
                Cr.BuyerCreativeID = Rs.getString("BuyerCreativeID");
                Cr.Height = Rs.getInt("Height");
                Cr.Width = Rs.getInt("Width");
                Cr.HtmlSnippet = Rs.getString("Snippet");
                Cr.Status = Rs.getString("Status");
                Cr.ClickThroughUrl = Rs.getString("ClickThroughURL");
                Creatives.add(Cr);

            }

            Rs.close();
            DBStatement.close();
            DBConnection.close();

        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (DBStatement != null) {
                    DBStatement.close();
                }
            } catch (SQLException se2) {
            }// nothing we can do
            try {
                if (DBConnection != null) {
                    DBConnection.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        return Creatives;
    }

    public List<Rule> FetchRules() {
        List<Rule> Rules = new ArrayList<Rule>();
        Connection DBConnection = null;
        Statement DBStatement = null;

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");

            DBConnection = DriverManager.getConnection(ConnectionUrl);
            DBStatement = DBConnection.createStatement();

            CallableStatement CStatement = DBConnection.prepareCall("{call Get_BiddingRules}");
            ResultSet Rs = CStatement.executeQuery();

            while (Rs.next()) {

                Rule R = new Rule();
                R.ID = Rs.getInt("ID");
                R.BannerWidth = Rs.getInt("BannerWidth");
                R.BannerHeight = Rs.getInt("BannerHeight");
                R.Country = Rs.getString("Country");
                R.Price = Rs.getFloat("Price");
                R.CreativeID = Rs.getInt("CreativeID");

                Rules.add(R);
            }

            Rs.close();
            DBStatement.close();
            DBConnection.close();

        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (DBStatement != null) {
                    DBStatement.close();
                }
            } catch (SQLException se2) {
            }// nothing we can do
            try {
                if (DBConnection != null) {
                    DBConnection.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        return Rules;
    }

}
