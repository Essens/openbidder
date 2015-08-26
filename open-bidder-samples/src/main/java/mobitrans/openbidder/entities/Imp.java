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

import javax.annotation.Generated;

import java.util.ArrayList;
import java.util.List;

@Generated("org.jsonschema2pojo")
public class Imp {


private String id;

private String displaymanager;

private String displaymanagerver;

private Double bidfloor;

private String bidfloorcur;

private Banner banner;

private Ext ext;

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
* The displaymanager
*/
public String getDisplaymanager() {
return displaymanager;
}

/**
* 
* @param displaymanager
* The displaymanager
*/
public void setDisplaymanager(String displaymanager) {
this.displaymanager = displaymanager;
}

/**
* 
* @return
* The displaymanagerver
*/
public String getDisplaymanagerver() {
return displaymanagerver;
}

/**
* 
* @param displaymanagerver
* The displaymanagerver
*/
public void setDisplaymanagerver(String displaymanagerver) {
this.displaymanagerver = displaymanagerver;
}

/**
* 
* @return
* The bidfloor
*/
public Double getBidfloor() {
return bidfloor;
}

/**
* 
* @param bidfloor
* The bidfloor
*/
public void setBidfloor(Double bidfloor) {
this.bidfloor = bidfloor;
}

/**
* 
* @return
* The bidfloorcur
*/
public String getBidfloorcur() {
return bidfloorcur;
}

/**
* 
* @param bidfloorcur
* The bidfloorcur
*/
public void setBidfloorcur(String bidfloorcur) {
this.bidfloorcur = bidfloorcur;
}

/**
* 
* @return
* The banner
*/
public Banner getBanner() {
return banner;
}

/**
* 
* @param banner
* The banner
*/
public void setBanner(Banner banner) {
this.banner = banner;
}

/**
* 
* @return
* The ext
*/
public Ext getExt() {
return ext;
}

/**
* 
* @param ext
* The ext
*/
public void setExt(Ext ext) {
this.ext = ext;
}

}
