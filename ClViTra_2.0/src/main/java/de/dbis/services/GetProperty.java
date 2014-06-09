/* ATLAS Group - Virtual Campfire - www.dbis.rwth-aachen.de
 * Copyright 2010-2012 Lehrstuhl Informatik V, RWTH Aachen, Germany. All Rights Reserved.
 */
package de.dbis.services;


/*
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

// TODO: Auto-generated Javadoc
/**
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates.
 */
public class GetProperty {

      /**
       * Gets the object.
       *
       * @param key the key
       * @param fileName the file name
       * @return the object
       */
      /*public static Object getObject(String key,String fileName) {          
        ResourceBundle resourceBundle = ResourceBundle.getBundle(fileName, Locale.getDefault());
        String objName = resourceBundle.getString(key);
        
        if (objName == null) {
            throw new RuntimeException("Implementation not supplied for"
                    + key+ " in"+ fileName+".properties");
        }
        try {
            return Class.forName(objName).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Factory unable to construct instance of "
                            + objName );
        }
    }*/
      
      /**
       * Gets the param.
       *
       * @param key the key
       * @param fileName the file name
       * @return the param
       */
      public static String getParam(String key,String fileName) {
    	  /*FileInputStream prop;
    	  ResourceBundle resourceBundle = null;
    	  //File try12 = new File("hello.txt");
    	  try {
    		  prop = new FileInputStream ("resources/"+fileName+".properties");
    		  resourceBundle = new PropertyResourceBundle(prop);
    	  } catch (FileNotFoundException e) {
    		  // TODO Auto-generated catch block
    		  e.printStackTrace();
    	  }
    	  catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
    	  }*/
    	  
          ResourceBundle resourceBundle = ResourceBundle .getBundle(fileName);
          String paramVal = resourceBundle.getString(key);
          
          if (paramVal == null) {
              throw new RuntimeException("Value For Parameter for " + key
                      + " not found in "+ fileName +" properties.");
          }
          return paramVal;
      }
      

}
