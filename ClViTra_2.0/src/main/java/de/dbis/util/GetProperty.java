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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
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
     * @throws MalformedURLException 
       */
      public static String getParam(String key,String fileName) {
    	  
    	  File file = new File("/etc/clvitra/");
    	  ClassLoader loader = null;
    	  try{
    		  URL[] urls = {file.toURI().toURL()};
    		  loader = new URLClassLoader(urls);
    	  } catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
    		  
    	  }
    	  
    	  ResourceBundle rb = ResourceBundle.getBundle(fileName, Locale.getDefault(), loader);
    	  String paramVal = rb.getString(key);
    	  
    	  /*PropertyResourceBundle prb = null;
    	  FileInputStream fis = null;
    	  try {
    		  fis = new FileInputStream("c:/temp/mybundle.txt");
    		  prb = new PropertyResourceBundle(fis);
    	  } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
    	    try {
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	  }
    	  
          //ResourceBundle resourceBundle = ResourceBundle .getBundle(fileName);
          //String paramVal = resourceBundle.getString(key);
          */
          
          
          if (paramVal == null) {
              throw new RuntimeException("Value For Parameter for " + key
                      + " not found in "+ fileName +" properties.");
          }
          return paramVal;
      }

}
