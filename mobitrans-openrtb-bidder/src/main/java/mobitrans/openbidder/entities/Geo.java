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
public class Geo {


private String country;

private Integer type;

/**
* 
* @return
* The country
*/
public String getCountry() {
return country;
}

/**
* 
* @param country
* The country
*/
public void setCountry(String country) {
this.country = country;
}

/**
* 
* @return
* The type
*/
public Integer getType() {
return type;
}

/**
* 
* @param type
* The type
*/
public void setType(Integer type) {
this.type = type;
}

}
