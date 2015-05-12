/*
 * @(#)BSDPackNameResolver.java
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
package com.nlbhub.packhound.fbsd;

import com.nlbhub.packhound.arch.UnTbz;
import com.nlbhub.packhound.bsd.BSDPackNameResolver;
import com.nlbhub.packhound.config.PackHoundParameters;
import net.javabeat.ftp.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.MalformedURLException;

/**
 * The FBSDPackNameResolver class.
 *
 * @author Anton P. Kolosov
 * @version 1.0
 */
public class FBSDPackNameResolver implements BSDPackNameResolver {
    /* Static variables begin ==> */
    private static Logger LOG = LoggerFactory.getLogger(FBSDPackNameResolver.class);
    /* <== Static variables end. */
    @Override
    public void retrieveIndex(boolean forceIndexReload, PackHoundParameters phParms) {
        LOG.info("Unpacking INDEX.bz2...");
        if (
                !forceIndexReload &&
                        UnTbz.unbz(
                                phParms.getPkgStorageDir() + "/INDEX.bz2",
                                phParms.getUnpackTempDir() + "/INDEX"
                        )
                ) {
            LOG.info("OK");
        } else {
            LOG.info("INDEX.bz2 not found or corrupted!");
            LOG.info("Downloading INDEX.bz2...");

            File fDest = new File(phParms.getPkgStorageDir(), "INDEX.bz2");
            try {
                FileUpload.download(
                        phParms.getProxy(),
                        phParms.getPkgSrcHost(),
                        null,
                        null,
                        "/INDEX.bz2", fDest,
                        phParms.getDownloadErrorTimeout());
                LOG.info("OK");
                retrieveIndex(false, phParms);
            } catch (MalformedURLException e1) {
                LOG.error("ERROR!");
                LOG.error(e1.getMessage());
            } catch (IOException e1) {
                LOG.error("ERROR!");
                LOG.error(e1.getMessage());
            }
        }
    }

    public String getFullPackageName(String pkgNameWithoutVersion, PackHoundParameters phParms) throws IOException {
        FileReader fileReader = (
                new FileReader(new File(phParms.getUnpackTempDir(), "INDEX"))
        );
        LineNumberReader lineNumberReader = new LineNumberReader(fileReader);
        String strCurLine = null;
        String strToSearch = null;
        boolean searchLike =  false;
        if (pkgNameWithoutVersion.endsWith("|")) {
            searchLike = true;
            strToSearch = (
                    pkgNameWithoutVersion.substring(
                            0, pkgNameWithoutVersion.length() - 1
                    )
            );
        } else {
            searchLike =  false;
            strToSearch = pkgNameWithoutVersion;
        }
        do {
            strCurLine = lineNumberReader.readLine();
            if (strCurLine != null) {
                int idx = strCurLine.indexOf("|");
                if (idx > 0) {
                    String fullPkgName = strCurLine.substring(0, idx);
                    if (searchLike) {
                        if (fullPkgName.matches(strToSearch)) {
                            return (fullPkgName + ".tbz");
                        }
                    } else {
                        int idx2 = fullPkgName.lastIndexOf("-");
                        if (idx2 > 0) {
                            String pkgCur = strCurLine.substring(0, idx2);
                            if (pkgCur.equals(strToSearch)) {
                                return fullPkgName + ".tbz";
                            }
                        }
                    }
                }
            }
        } while (strCurLine != null);
        return null;
    }
}
