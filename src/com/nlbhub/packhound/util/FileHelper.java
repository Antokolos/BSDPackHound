/*
 * @(#)FileHelper.java
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
package com.nlbhub.packhound.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * The FileHelper class contains useful file operations. 
 *
 * @author Anton P. Kolosov
 * @version 1.0
 */
public class FileHelper {
    /** Maximum block size */
    private static final int BLOCK_SIZE = 1024;
    
    /* Constructors begin ==> */
    /**
     * Creating FileHelper
     */
    protected FileHelper() {
        super();
        // TODO Auto-generated constructor stub
    }
    /* <== Constructors end. */
    /* Methods begin ==> */
    /**
     * Returns a specified file as a string
     *
     * @param fName file name from classpath.
     *
     * @return String representation for the specified file or
     * <tt>null</tt> if file is not found.
     */
    public static String getFileAsString(String fName) {
        
        InputStream strm = (
            FileHelper.class.getClassLoader().getResourceAsStream(fName)
        );
        
        return getFileAsString(strm);
    }
    
    /**
     * Returns a specified file as a string
     *
     * @param strm input file stream.
     *
     * @return String representation for the specified file or
     * <tt>null</tt> if file is not found.
     */
    public static String getFileAsString(InputStream strm) {
        
        if (strm != null) {
            StringBuilder sb = new StringBuilder();
            byte[] bytes = new byte[BLOCK_SIZE];
            int iBytesRead;
            try {
                while (true) {
                    iBytesRead = strm.read(bytes);
                    if (iBytesRead <= 0) break;
                    String str = new String(bytes, 0, iBytesRead);
                    sb.append(str);
                }
            } catch (IOException e) {
                return null;
            }
            
            return sb.toString();
        } else {
            return null;
        }
    }
    
    /**
     * Copies specified file to the target location.
     *
     * @param sourcePath source file path.
     * @param targetPath destination file path.
     * @throws IOException if an I/O error occurs.
     */
    public static void transfer(
        File sourcePath, File targetPath
    ) throws IOException {

        FileInputStream sourceStream = new FileInputStream(sourcePath);

        try {
            writeFile(targetPath, sourceStream);
        } finally {
            sourceStream.close();
        }
    }

    /**
     * Transfers content from input stream to the output stream.
     *
     * @param input input stream whose content is to be transferred.
     * @param output output stream output which content is to be transferred.
     * @throws IOException if an I/O error occurs.
     */
    public static void transfer(
        InputStream input, OutputStream output
    ) throws IOException {
        byte[] buffer = new byte[BLOCK_SIZE];

        for (
            int bytesRead = input.read(buffer);
            bytesRead != -1;
            bytesRead = input.read(buffer)
        ) {
            output.write(buffer, 0, bytesRead);
        }
    }
    
    /**
     * Writes content from the specified input stream to the specified file.
     *
     * @param file the file to be opened for writing.
     * @param input input stream whose content is to be written to the file.
     *
     * @throws IOException if an I/O error occurs.
     */
    public static void writeFile(
        File file, InputStream input
    ) throws IOException {
        FileOutputStream output = new FileOutputStream(file);

        try {
            transfer(input, output);
        } finally {
            output.close();
        }
    }

    public static boolean createDirIfNotExists(String dirPath) throws IOException {
        File dir = new File(dirPath);
        return !dir.exists() && dir.mkdirs();
    }
}
