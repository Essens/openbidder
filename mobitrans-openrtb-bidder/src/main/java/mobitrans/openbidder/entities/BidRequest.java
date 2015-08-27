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

//-----------------------------------mobitrans.openbidder.entities.BidRequest.java-----------------------------------

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;


@Generated("org.jsonschema2pojo")
public class BidRequest {


private String id;

private Integer at;

private Integer tmax;

private List<String> cur = new ArrayList<String>();

private List<String> bcat = new ArrayList<String>();

private List<Imp> imp = new ArrayList<Imp>();

private Site site;

private Device device;

private User user;

private Ext_ ext;

/**
* 
* @return
* The id
*/
public String getId() {
return id;
}

/**
* 
* @param id
* The id
*/
public void setId(String id) {
this.id = id;
}

/**
* 
* @return
* The at
*/
public Integer getAt() {
return at;
}

/**
* 
* @param at
* The at
*/
public void setAt(Integer at) {
this.at = at;
}

/**
* 
* @return
* The tmax
*/
public Integer getTmax() {
return tmax;
}

/**
* 
* @param tmax
* The tmax
*/
public void setTmax(Integer tmax) {
this.tmax = tmax;
}

/**
* 
* @return
* The cur
*/
public List<String> getCur() {
return cur;
}

/**
* 
* @param cur
* The cur
*/
public void setCur(List<String> cur) {
this.cur = cur;
}

/**
* 
* @return
* The bcat
*/
public List<String> getBcat() {
return bcat;
}

/**
* 
* @param bcat
* The bcat
*/
public void setBcat(List<String> bcat) {
this.bcat = bcat;
}

/**
* 
* @return
* The imp
*/
public List<Imp> getImp() {
return imp;
}

/**
* 
* @param imp
* The imp
*/
public void setImp(List<Imp> imp) {
this.imp = imp;
}

/**
* 
* @return
* The site
*/
public Site getSite() {
return site;
}

/**
* 
* @param site
* The site
*/
public void setSite(Site site) {
this.site = site;
}

/**
* 
* @return
* The device
*/
public Device getDevice() {
return device;
}

/**
* 
* @param device
* The device
*/
public void setDevice(Device device) {
this.device = device;
}

/**
* 
* @return
* The user
*/
public User getUser() {
return user;
}

/**
* 
* @param user
* The user
*/
public void setUser(User user) {
this.user = user;
}

/**
* 
* @return
* The ext
*/
public Ext_ getExt() {
return ext;
}

/**
* 
* @param ext
* The ext
*/
public void setExt(Ext_ ext) {
this.ext = ext;
}

}
