/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobitrans.openbidder.entities;

/**
 *
 * @author Aakif
 */
//-----------------------------------mobitrans.openbidder.entities.Device.java-----------------------------------


import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;

@Generated("org.jsonschema2pojo")
public class Device {


private Integer dnt;

private String ua;

private String ip;

private Geo geo;

private String dpidsha1;

private String dpidmd5;

private String make;

private String model;

private String os;

private String osv;

private Integer connectiontype;

/**
* 
* @return
* The dnt
*/
public Integer getDnt() {
return dnt;
}

/**
* 
* @param dnt
* The dnt
*/
public void setDnt(Integer dnt) {
this.dnt = dnt;
}

/**
* 
* @return
* The ua
*/
public String getUa() {
return ua;
}

/**
* 
* @param ua
* The ua
*/
public void setUa(String ua) {
this.ua = ua;
}

/**
* 
* @return
* The ip
*/
public String getIp() {
return ip;
}

/**
* 
* @param ip
* The ip
*/
public void setIp(String ip) {
this.ip = ip;
}

/**
* 
* @return
* The geo
*/
public Geo getGeo() {
return geo;
}

/**
* 
* @param geo
* The geo
*/
public void setGeo(Geo geo) {
this.geo = geo;
}

/**
* 
* @return
* The dpidsha1
*/
public String getDpidsha1() {
return dpidsha1;
}

/**
* 
* @param dpidsha1
* The dpidsha1
*/
public void setDpidsha1(String dpidsha1) {
this.dpidsha1 = dpidsha1;
}

/**
* 
* @return
* The dpidmd5
*/
public String getDpidmd5() {
return dpidmd5;
}

/**
* 
* @param dpidmd5
* The dpidmd5
*/
public void setDpidmd5(String dpidmd5) {
this.dpidmd5 = dpidmd5;
}

/**
* 
* @return
* The make
*/
public String getMake() {
return make;
}

/**
* 
* @param make
* The make
*/
public void setMake(String make) {
this.make = make;
}

/**
* 
* @return
* The model
*/
public String getModel() {
return model;
}

/**
* 
* @param model
* The model
*/
public void setModel(String model) {
this.model = model;
}

/**
* 
* @return
* The os
*/
public String getOs() {
return os;
}

/**
* 
* @param os
* The os
*/
public void setOs(String os) {
this.os = os;
}

/**
* 
* @return
* The osv
*/
public String getOsv() {
return osv;
}

/**
* 
* @param osv
* The osv
*/
public void setOsv(String osv) {
this.osv = osv;
}

/**
* 
* @return
* The connectiontype
*/
public Integer getConnectiontype() {
return connectiontype;
}

/**
* 
* @param connectiontype
* The connectiontype
*/
public void setConnectiontype(Integer connectiontype) {
this.connectiontype = connectiontype;
}

}
