/*
 * @(#)UnTbz.java
 *
 * Copyright (c) 2015, Anton P. Kolosov
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of FBSDPackHound nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.nlbhub.packhound.arch;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.nlbhub.packhound.fbsd.PackHoundParameters;
import org.apache.tools.bzip2.*;
import org.apache.tools.tar.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The UnTbz class. 
 *
 * @author Anton P. Kolosov
 * @version 1.0
 */
public class UnTbz {
    private static Logger LOG = LoggerFactory.getLogger(UnTbz.class);
    final static int BUFFER = 2048;
    public static boolean unTbzFBSDPackageContents(
        PackHoundParameters phParms, String zipFName
    ) { 
        StringBuilder sbOutFName = new StringBuilder();
        sbOutFName.append(phParms.getUnpackTempDir()).append("/");
        sbOutFName.append(phParms.getUnpackTempFile());
        StringBuilder sbInFName = new StringBuilder();
        sbInFName.append(phParms.getPkgStorageDir()).append("/");
        sbInFName.append(zipFName);
        if (!unbz(
            sbInFName.toString(), sbOutFName.toString()
        )) return false;
        if (!unTarFBSDPackageContents(
            sbOutFName.toString(), phParms.getUnpackTempDir() + "/"
        )) return false;
        return true;
    }
    public static boolean unbz(String zipFName, String outFName) {
        try {
            BufferedOutputStream dest = null;
            FileInputStream fis = ( 
                new FileInputStream(zipFName)
            );

            // You have to read two bytes before passing 
            // the stream to CBZip2InputStream 
            fis.read();
            fis.read();

            CBZip2InputStream zis = (
                new CBZip2InputStream(new BufferedInputStream(fis))
            );
            
            boolean flag = true;
            while (flag) {
                int count;
                byte data[] = new byte[BUFFER];
                
                // write the files to the disk
                FileOutputStream fos = (
                    new FileOutputStream(outFName)
                );

                dest = new BufferedOutputStream(fos, BUFFER);

                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                
                dest.flush();
                dest.close();
                flag = false;    // Only one iteration
            }
            zis.close();
            return true;
        } catch(Exception e) {
            LOG.error(e.getMessage());
            return false;
        }
    }
    
    public static boolean unTarFBSDPackageContents(
        String zipFName, String tempDirName
    ) {
        try {
            BufferedOutputStream dest = null;
            FileInputStream fis = ( 
                new FileInputStream(zipFName)
            );

            TarInputStream tis = (
                new TarInputStream(new BufferedInputStream(fis))
            );

            TarEntry entry;
            
            while ((entry = tis.getNextEntry()) != null) {
                if (entry.getName().equals("+CONTENTS")) {
                    LOG.info("Extracting: " + entry.getName());
                    int count;
                    byte data[] = new byte[BUFFER];

                    // write the files to the disk
                    FileOutputStream fos = (
                        new FileOutputStream(tempDirName + entry.getName())
                    );

                    dest = new BufferedOutputStream(fos, BUFFER);

                    while ((count = tis.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    
                    dest.flush();
                    dest.close();
                    break;
                }
            }
            tis.close();
            return true;
        } catch(Exception e) {
            LOG.error(e.getMessage());
            return false;
        }
    }
}
