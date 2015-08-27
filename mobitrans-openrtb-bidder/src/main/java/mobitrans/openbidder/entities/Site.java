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


import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;


@Generated("org.jsonschema2pojo")
public class Site {


private String id;

private String name;

private String domain;

private List<String> cat = new ArrayList<String>();

private Publisher publisher;

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
* The name
*/
public String getName() {
return name;
}

/**
* 
* @param name
* The name
*/
public void setName(String name) {
this.name = name;
}

/**
* 
* @return
* The domain
*/
public String getDomain() {
return domain;
}

/**
* 
* @param domain
* The domain
*/
public void setDomain(String domain) {
this.domain = domain;
}

/**
* 
* @return
* The cat
*/
public List<String> getCat() {
return cat;
}

/**
* 
* @param cat
* The cat
*/
public void setCat(List<String> cat) {
this.cat = cat;
}

/**
* 
* @return
* The publisher
*/
public Publisher getPublisher() {
return publisher;
}

/**
* 
* @param publisher
* The publisher
*/
public void setPublisher(Publisher publisher) {
this.publisher = publisher;
}

}
