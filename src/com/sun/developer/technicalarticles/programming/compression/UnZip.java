/*
 * @(#)UnZip.java
 *
 * Copyright (c) 
 * http://java.sun.com/developer/technicalArticles/Programming/compression/
 */
package com.sun.developer.technicalarticles.programming.compression;

import java.io.*;
import java.util.zip.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnZip {
    private static Logger LOG = LoggerFactory.getLogger(UnZip.class);
    
    final static int BUFFER = 2048;
    public static boolean uzip (String zipFName) {
        boolean result = false;
        try {
            BufferedOutputStream dest = null;
            FileInputStream fis = ( 
                new FileInputStream(zipFName)
            );

            ZipInputStream zis = (
                new ZipInputStream(new BufferedInputStream(fis))
            );

            ZipEntry entry;
            
            while ((entry = zis.getNextEntry()) != null) {
                LOG.info("Extracting: " + entry);
                int count;
                byte data[] = new byte[BUFFER];

                // write the files to the disk
                FileOutputStream fos = new FileOutputStream(entry.getName());

                dest = new BufferedOutputStream(fos, BUFFER);

                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                
                dest.flush();
                dest.close();
            }
            zis.close();
            result = true;
        } catch(Exception e) {
            LOG.error(e.getMessage());
        }
        
        return result;
    }
}
