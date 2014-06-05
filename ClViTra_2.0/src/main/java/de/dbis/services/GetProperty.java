/* ATLAS Group - Virtual Campfire - www.dbis.rwth-aachen.de
 * Copyright © 2010-2012 Lehrstuhl Informatik V, RWTH Aachen, Germany. All Rights Reserved.
 */
package de.dbis.services;


/*
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */


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
      public static Object getObject(String key,String fileName) {          
        ResourceBundle resourceBundle = ResourceBundle.getBundle(fileName);
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
    }
      
      /**
       * Gets the param.
       *
       * @param key the key
       * @param fileName the file name
       * @return the param
       */
      public static String getParam(String key,String fileName) {
          ResourceBundle resourceBundle = ResourceBundle .getBundle(fileName);
          String paramVal = resourceBundle.getString(key);
          
          if (paramVal == null) {
              throw new RuntimeException("Value For Parameter for " + key
                      + " not found in "+ fileName +" properties.");
          }
          return paramVal;
      }
      

}
