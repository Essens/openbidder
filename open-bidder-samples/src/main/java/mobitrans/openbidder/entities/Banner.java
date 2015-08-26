/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Aakif
 */

//-----------------------------------mobitrans.openbidder.entities.Banner.java-----------------------------------

package mobitrans.openbidder.entities;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;


@Generated("org.jsonschema2pojo")
public class Banner {


private Integer w;

private Integer h;

private String id;

private List<Integer> btype = new ArrayList<Integer>();

private List<Integer> battr = new ArrayList<Integer>();

private Integer hmax;

private Integer wmax;

/**
* 
* @return
* The w
*/
public Integer getW() {
return w;
}

/**
* 
* @param w
* The w
*/
public void setW(Integer w) {
this.w = w;
}

/**
* 
* @return
* The h
*/
public Integer getH() {
return h;
}

/**
* 
* @param h
* The h
*/
public void setH(Integer h) {
this.h = h;
}

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
* The btype
*/
public List<Integer> getBtype() {
return btype;
}

/**
* 
* @param btype
* The btype
*/
public void setBtype(List<Integer> btype) {
this.btype = btype;
}

/**
* 
* @return
* The battr
*/
public List<Integer> getBattr() {
return battr;
}

/**
* 
* @param battr
* The battr
*/
public void setBattr(List<Integer> battr) {
this.battr = battr;
}

/**
* 
* @return
* The hmax
*/
public Integer getHmax() {
return hmax;
}

/**
* 
* @param hmax
* The hmax
*/
public void setHmax(Integer hmax) {
this.hmax = hmax;
}

/**
* 
* @return
* The wmax
*/
public Integer getWmax() {
return wmax;
}

/**
* 
* @param wmax
* The wmax
*/
public void setWmax(Integer wmax) {
this.wmax = wmax;
}

}
