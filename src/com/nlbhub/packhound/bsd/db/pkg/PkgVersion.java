/*
 * @(#)PkgVersion.java
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
package com.nlbhub.packhound.bsd.db.pkg;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nlbhub.packhound.util.FileHelper;

/**
 * 
 * The PkgVersion class stores the version of the package.
 * The version is stored as the simple array of digits.
 * Each digit represent major, middle, minor etc subversions.
 *
 * @author Anton P. Kolosov
 * @version 1.0
 */
public class PkgVersion implements Comparable<PkgVersion> {
    /* Static variables begin ==> */
    private static Logger LOG = LoggerFactory.getLogger(PkgVersion.class);
    /* <== Static variables end. */

    /* Instance variables begin ==> */
    private int[] m_versionArray = null;
    private int m_subversion = 0;
    /* <== Instance variables end. */

    /* Static blocks begin ==> */
    /* <== Static blocks end. */

    /* Constructors begin ==> */
    /**
     * Creating PkgVersion
     */
    public PkgVersion() {
        super();
        // TODO Auto-generated constructor stub
    }
    /* <== Constructors end. */
    /* Methods begin ==> */

    /**
     * Initializes the class by string representation of the version
     * 
     * @param versionString string representation of the version, in
     * format 1.2.3_4. Here 4 is the subversion
     */
    public void init(String versionString) throws Exception {
        if (m_versionArray != null) {
            throw new Exception("Package version is already initialized.");
        }
        String[] versionParts = versionString.split(".|,");
        int versionComponentCount = versionParts.length;
        
        if (versionComponentCount >= 1) {
            m_versionArray = new int[versionComponentCount];
            for (int i = 0; i < versionComponentCount; i++) {
                try {
                    m_versionArray[i] = Integer.parseInt(versionParts[i]);
                } catch (NumberFormatException e) {
                    String[] subversionParts = versionParts[i].split("_");
                    if (subversionParts.length == 2) {
                        try {
                            m_versionArray[i] = Integer.parseInt(
                                subversionParts[0]
                            );
                        } catch (NumberFormatException e1) {
                            throw new Exception(
                                "Error while package subversion "
                                + "initialization: " + e1.getMessage()
                            );
                        }
                        try {
                            m_subversion = Integer.parseInt(
                                subversionParts[1]
                            );
                        } catch (NumberFormatException e1) {
                            throw new Exception(
                                "Error while package subversion "
                                + "initialization: " + e1.getMessage()
                            );
                        }
                    } else {
                        throw new Exception(
                            "Error while package version initialization: "
                            + e.getMessage()
                        );
                    }
                }
            }
        } else {
            throw new Exception("Number of version components is wrong.");
        }
    }
    
    /**
     * Initializes the class from input stream, e.g. a file
     * 
     * @param strm input stream containing string representation of version
     */
    public void init(InputStream strm) throws Exception {
        init(FileHelper.getFileAsString(strm));
    }

    /**
     * Returns string representation
     * @return String representation of object
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int lastElem = m_versionArray.length - 1;
        for (int i = 0; i < lastElem; i++) {
            sb.append(m_versionArray[i]).append(".");
        }
        sb.append(m_versionArray[lastElem]).append("_").append(m_subversion);
        return sb.toString();
    }

    /**
     * Compares two version objects
     * @param o object to compare to
     * @return 0 if objects are equal, -1, if this object is less than o,
     * 1 if this object is greater than o
     */
    public int compareTo(PkgVersion o) {
        if (o == this) return 0;
        if (!(o instanceof PkgVersion)) throw new ClassCastException();
        if ((this.m_versionArray == null) && (o.m_versionArray != null)) {
            return -1;
        } else if (
            (this.m_versionArray == null) && (o.m_versionArray == null)
        ) {
            return 0;
        } else if (
            (this.m_versionArray != null) && (o.m_versionArray == null)
        ) {
            return 1;
        } else {
            int shortestLength = m_versionArray.length;
            if (o.m_versionArray.length < shortestLength) {
                shortestLength = o.m_versionArray.length;
            }
            
            for (int i = 0; i < shortestLength; i++) {
                if (m_versionArray[i] > o.m_versionArray[i]) {
                    return 1;
                } else if (m_versionArray[i] < o.m_versionArray[i]) {
                    return -1;
                }
            }
            
            if (m_versionArray.length > shortestLength) {
                return 1;
            } else if (m_versionArray.length > shortestLength) {
                return -1;
            } 
        }
        
        if (m_subversion < o.m_subversion) {
            return -1;
        } else if (m_subversion > o.m_subversion) {
            return 1;
        }
        return 0;
    }
    /* <== Methods end. */
}
