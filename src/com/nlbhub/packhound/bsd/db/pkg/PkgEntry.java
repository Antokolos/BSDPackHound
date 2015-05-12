/*
 * @(#)PkgEntry.java
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

import com.nlbhub.packhound.config.PackHoundParameters;
import com.nlbhub.packhound.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 *
 * The PkgEntry class.
 *
 * @author Anton P. Kolosov
 * @version 1.0
 */
public abstract class PkgEntry {
    /* Static variables begin ==> */
    private static Logger LOG = LoggerFactory.getLogger(PkgEntry.class);
    /* <== Static variables end. */

    /* Instance variables begin ==> */
    private String m_strPackageDirName;
    /* <== Instance variables end. */

    /* Static blocks begin ==> */
    /* <== Static blocks end. */

    /* Constructors begin ==> */
    /**
     * Creating PkgEntry
     */
    protected PkgEntry() {
        super();
        // TODO Auto-generated constructor stub
    }
    /* <== Constructors end. */
    /* Methods begin ==> */

    /**
     * Initializes PkgVersion, creates corresponding directory in 
     * package database dir
     * @param strPackageFileName name of THIS package
     * @param phParms program parameters
     * @return <TT>true</TT> if +CONTENTS file already exists in pkg db or
     * has been successfully extracted from package file, <TT>false</TT>
     * otherwise.
     */
    public boolean init(
            String strPackageFileName, PackHoundParameters phParms
    ) throws Exception {
        int iExtensionPos = strPackageFileName.lastIndexOf(".");
        m_strPackageDirName = (
                strPackageFileName.substring(0, iExtensionPos)
        );
        File pkgDir = (
                new File(phParms.getPkgDatabaseDir(), m_strPackageDirName)
        );

        if (pkgDir.isDirectory()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Package dir for ").append(strPackageFileName);
            sb.append(" already exists.");
            LOG.info(sb.toString());
            return true;
        } else {

            StringBuilder sb = new StringBuilder();
            sb.append("Trying to read ").append(strPackageFileName);
            sb.append("... ");
            LOG.info(sb.toString());
            if (unpackPackageContents(phParms, strPackageFileName)) {
                boolean result = pkgDir.mkdir();
                if (result) {
                    StringBuilder sb1 = new StringBuilder();
                    sb1.append(phParms.getUnpackTempDir()).append("/+CONTENTS");
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append(phParms.getPkgDatabaseDir());
                    sb2.append("/").append(m_strPackageDirName);
                    sb2.append("/+CONTENTS");
                    File contentsFileInTmp = new File(sb1.toString());
                    File contentsFileInPkg = new File(sb2.toString());
                    FileHelper.transfer(contentsFileInTmp, contentsFileInPkg);
                    return true;
                } else {
                    throw new Exception(
                            "Cannot create package entry dir in pkg db"
                    );
                }
            } else {
                return false;
            }
        }
    }

    public abstract boolean unpackPackageContents(PackHoundParameters phParms, String zipFName);

    public String getPackageDirName() {
        return m_strPackageDirName;
    }

    public PkgVersion getPkgVersion() {
        String[] pkgDirNameComponents = m_strPackageDirName.split("-");
        int iComponentsCount = pkgDirNameComponents.length;
        if (iComponentsCount > 0) {
            PkgVersion ver = new PkgVersion();
            try {
                ver.init(pkgDirNameComponents[iComponentsCount - 1]);
                return ver;
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }
    /* <== Methods end. */
}
