/*
 * @(#)FileUpload.java
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
package net.javabeat.ftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to upload a file to a FTP server.
 * Originally written by Muthu, slightly modified by Anton P. Kolosov
 * (added logging and proxy support).
 * 
 * @author Muthu
 */
public class FileUpload {
    private static Logger LOG = LoggerFactory.getLogger(FileUpload.class);
    
    /**
    * Upload a file to a FTP server. A FTP URL is generated with the
    * following syntax:
    * ftp://user:password@host:port/filePath;type=i.
    * 
    * @param ftpServer , FTP server address (optional port ':portNumber').
    * @param user , Optional user name to login.
    * @param password , Optional password for user.
    * @param fileName , Destination file name on FTP server (with optional
    *            preceding relative path, e.g. "myDir/myFile.txt").
    * @param source , Source file to upload.
    * @throws MalformedURLException, IOException on error.
    */
    public static void upload( String ftpServer, String user, String password,
        String fileName, File source 
    ) throws MalformedURLException, IOException {
        if (ftpServer != null && fileName != null && source != null) {
            StringBuffer sb = new StringBuffer( "ftp://" );
            // check for authentication else assume its anonymous access.
            if (user != null && password != null) {
                sb.append( user );
                sb.append( ':' );
                sb.append( password );
                sb.append( '@' );
            }
            
            sb.append( ftpServer );
            sb.append( '/' );
            sb.append( fileName );
            /*
             * type ==> a=ASCII mode, i=image (binary) mode, d= file directory
             * listing
             */
            sb.append( ";type=i" );

            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            try {
                URL url = new URL(sb.toString());
                URLConnection urlc = url.openConnection();

                bos = new BufferedOutputStream(urlc.getOutputStream());
                bis = new BufferedInputStream(new FileInputStream( source ));

                int i;
                // read byte by byte until end of stream
                while ((i = bis.read()) != -1) {
                    bos.write( i );
                }
            } finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException ioe) {
                        LOG.error(ioe.getMessage());
                    }
                }
                
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
        } else {
            LOG.info("Input not available.");
        }
    }

   /**
    * Download a file from a FTP server. A FTP URL is generated with the
    * following syntax:
    * ftp://user:password@host:port/filePath;type=i.
    * 
    * @param ftpServer , FTP server address (optional port ':portNumber').
    * @param user , Optional user name to login.
    * @param password , Optional password for user.
    * @param fileName , Name of file to download (with optional preceeding
    *            relative path, e.g. one/two/three.txt).
    * @param destination , Destination file to save.
    * @param downloadErrorTimeout download error timeout
    * @throws MalformedURLException, IOException on error.
    */
    public static void download(
        Proxy proxy, String ftpServer, String user, String password,
        String fileName, File destination, int downloadErrorTimeout
    ) throws MalformedURLException, IOException {
        if (ftpServer != null && fileName != null && destination != null) {
            StringBuffer sb = new StringBuffer("ftp://");
            // check for authentication else assume its anonymous access.
            if (user != null && password != null) {
                sb.append( user );
                sb.append( ':' );
                sb.append( password );
                sb.append( '@' );
            }
            
            sb.append( ftpServer );
            sb.append( '/' );
            sb.append( fileName );
            /*
             * type ==> a=ASCII mode, i=image (binary) mode, d= file directory
             * listing
             */
            sb.append( ";type=i" );
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            String address = sb.toString();
            URL url = new URL(address);
            URLConnection urlc = null;
            
            boolean workInProgress = true;
            do {
                try {
                    if (proxy != null) {
                        urlc = url.openConnection(proxy);
                    } else {
                        urlc = url.openConnection();
                    }
                    
                    if (urlc != null) {
                        urlc.connect();
        
                        bis = new BufferedInputStream(urlc.getInputStream());
                        /*bos = new BufferedOutputStream( new FileOutputStream(
                              destination.getName() ) );*/
                        bos = new BufferedOutputStream(
                            new FileOutputStream(destination)
                        );// Anton P. Kolosov
        
                        int i;
                        while ((i = bis.read()) != -1) {
                            bos.write( i );
                        }
                        
                        workInProgress = false;
                    } else {
                        LOG.error(
                            "Process aborted. Cannot connect to " + address
                        );
                        workInProgress = false;
                    }
                } catch (IOException e) {
                    // Failures with proxied connection occurs without this sleep
                    try {
                        LOG.error(e.getMessage() + " Sleeping for a while...");
                        Thread.sleep(downloadErrorTimeout);
                    } catch (InterruptedException e1) {
                        // ignore
                    }
                } finally {
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                       
                    if (bos != null) {
                        try {
                            bos.close();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                }
            } while (workInProgress);
        } else {
            LOG.info( "Input not available" );
        }
    }
}
