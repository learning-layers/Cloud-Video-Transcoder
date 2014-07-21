/* ATLAS Group - Virtual Campfire - www.dbis.rwth-aachen.de
 * Copyright 2010-2012 Lehrstuhl Informatik V, RWTH Aachen, Germany. All Rights Reserved.
 */
package de.dbis.util;


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
 * Read and returns the configurations from .properties files.
 * 
 */
public class GetProperty {

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
