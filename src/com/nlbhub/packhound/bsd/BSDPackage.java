/*
 * @(#)BSDPackage.java
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
package com.nlbhub.packhound.bsd;

import com.nlbhub.packhound.bsd.db.pkg.PkgEntry;
import com.nlbhub.packhound.bsd.db.pkg.PkgVersion;
import com.nlbhub.packhound.config.PackHoundParameters;
import net.javabeat.ftp.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.util.ArrayList;


/**
 * The BSDPackage class.
 *
 * @author Anton P. Kolosov
 * @version 1.0
 */
public abstract class BSDPackage implements Comparable {
    /* Static variables begin ==> */
    private static Logger LOG = LoggerFactory.getLogger(BSDPackage.class);
    /* <== Static variables end. */

    /* Instance variables begin ==> */
    private String m_strPackageFileName;
    private ArrayList<BSDPackage> m_lstPkgDeps;
    /* <== Instance variables end. */

    /* Static blocks begin ==> */
    /* <== Static blocks end. */

    /* Constructors begin ==> */

    /**
     * Creating FBSDPackage
     */
    protected BSDPackage() {
        super();
        m_strPackageFileName = "";
        m_lstPkgDeps = new ArrayList<BSDPackage>();
    }
    /* <== Constructors end. */
    /* Methods begin ==> */

    @Override
    public int compareTo(Object o) {
        int compareResult = -1;
        if (o instanceof BSDPackage) {
            BSDPackage pkg = (BSDPackage) o;
            for (BSDPackage dep : getPkgDeps()) {
                if (dep.getPackageFileName().equalsIgnoreCase(pkg.getPackageFileName())) {
                    return 1;
                }
            }
            for (BSDPackage dep : getPkgDeps()) {
                int localResult = dep.compareTo(pkg);
                if (localResult > compareResult) {
                    compareResult = localResult;
                }
            }
        }
        return compareResult;
    }

    /**
     * Gets m_strPackageFileName
     *
     * @return the m_strPackageFileName
     */
    public String getPackageFileName() {
        return m_strPackageFileName;
    }

    /**
     * Sets m_strPackageFileName
     *
     * @param mStrPackageFileName the m_strPackageFileName to set
     */
    public void setPackageFileName(String mStrPackageFileName) {
        m_strPackageFileName = mStrPackageFileName;
    }

    /**
     * Gets m_lstPkgDeps
     *
     * @return the m_lstPkgDeps
     */
    public ArrayList<BSDPackage> getPkgDeps() {
        return m_lstPkgDeps;
    }

    /**
     * Sets m_lstPkgDeps
     *
     * @param mLstPkgDeps the m_lstPkgDeps to set
     */
    public void setPkgDeps(ArrayList<BSDPackage> mLstPkgDeps) {
        m_lstPkgDeps = mLstPkgDeps;
    }

    /**
     * Initializes FBSDPackage by reading +CONTENTS file from temp folder
     *
     * @param strPackageFileName name of THIS package
     * @param phParms            program parameters
     */
    public void init(
            String strPackageFileName,
            String strBasePackageFileName,
            PackHoundParameters phParms
    ) {
        LineNumberReader lineNumberReader = null;
        FileReader fileReader = null;
        FileWriter fwrRequiredBy = null;
        FileReader frReq = null;
        LineNumberReader lnrReq = null;
        String strCurLine;

        m_strPackageFileName = strPackageFileName;

        try {
            PkgEntry pkgEntry = newPkgEntry();
            if (pkgEntry.init(strPackageFileName, phParms)) {
                StringBuilder sb = new StringBuilder();
                sb.append(phParms.getPkgDatabaseDir()).append("/");
                sb.append(pkgEntry.getPackageDirName()).append("/+REQUIRED_BY");
                File fRequiredBy = new File(sb.toString());

                boolean needToInclude = true;
                if (fRequiredBy.isFile()) {
                    frReq = new FileReader(sb.toString());
                    lnrReq = (
                            new LineNumberReader(frReq)
                    );

                    while ((strCurLine = lnrReq.readLine()) != null) {
                        if (strCurLine.equals(strBasePackageFileName)) {
                            needToInclude = false;
                            break;
                        }
                    }
                } else {
                    fRequiredBy.createNewFile();
                }

                if (needToInclude && (strBasePackageFileName != null)) {
                    fwrRequiredBy = new FileWriter(fRequiredBy, true);
                    fwrRequiredBy.append(strBasePackageFileName);
                    fwrRequiredBy.append(PackHoundParameters.getNewline());
                }

                StringBuilder sbc = new StringBuilder();
                sbc.append(phParms.getPkgDatabaseDir()).append("/");
                sbc.append(pkgEntry.getPackageDirName()).append("/+CONTENTS");
                fileReader = new FileReader(sbc.toString());
                lineNumberReader = (
                        new LineNumberReader(fileReader)
                );

                while ((strCurLine = lineNumberReader.readLine()) != null) {
                    BSDPackage pkgDepCur = getDependency(strCurLine);
                    if (pkgDepCur != null) {
                        m_lstPkgDeps.add(pkgDepCur);
                    }
                }
            } else {
                LOG.warn(
                        m_strPackageFileName + " not found or damaged!"
                );
                /*try {
                    Process ps = Runtime.getRuntime().exec(
                        //"cmd /c start iexplore http://ftp.freebsd.org/pub/"
                        //+ "FreeBSD/ports/packages/All/" + strPackageFileName
                        //"cmd /c start /MIN "
                        "cmd /c start /WAIT /MIN "
                        + "iexplore http://ftp.freebsd.org/pub/"
                        + "FreeBSD/ports/packages/All/" + strPackageFileName
                    );
                    ps.waitFor();
                }
                catch(IOException e) {
                }
                catch(InterruptedException e) {
                }*/
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Downloading file ").append(m_strPackageFileName);
                sb2.append("... ");
                StringBuilder sbFNDest = new StringBuilder();
                sbFNDest.append(phParms.getPkgStorageDir()).append("/");
                sbFNDest.append(m_strPackageFileName);
                LOG.info(sb2.toString());
                File fDest = new File(sbFNDest.toString());
                try {
                    FileUpload.download(
                            phParms.getProxy(),
                            phParms.getPkgSrcHost(),
                            null,
                            null,
                            strPackageFileName, fDest,
                            phParms.getDownloadErrorTimeout());
                    LOG.info("OK");
                    init(strPackageFileName, strBasePackageFileName, phParms);
                } catch (MalformedURLException e1) {
                    LOG.error("ERROR!");
                    LOG.error(e1.getMessage());
                } catch (IOException e1) {
                    LOG.error("ERROR!");
                    LOG.error(e1.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            LOG.error(e.getMessage());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            LOG.error(e.getMessage());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            LOG.error(e.getMessage());
        } finally {
            try {
                if (lineNumberReader != null) lineNumberReader.close();
                if (fileReader != null) fileReader.close();
                if (fwrRequiredBy != null) fwrRequiredBy.close();
                if (lnrReq != null) lnrReq.close();
                if (frReq != null) frReq.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                LOG.error(e.getMessage());
            }
        }
    }

    protected abstract PkgEntry newPkgEntry();

    protected abstract BSDPackage getDependency(String curLine);

    public void setStrPackageFileName(String strPackageFileName) {
        m_strPackageFileName = strPackageFileName;
    }

    /**
     * Return a string representation.
     * DO NOT acts recursively!
     *
     * @return String representation
     */
    @Override
    public String toString() {
        String strFBSDPackInfo = (
                String.format(
                        "%s -> ", m_strPackageFileName
                )
        );

        if (m_lstPkgDeps.isEmpty()) {
            strFBSDPackInfo += "NONE\r\n";
            return strFBSDPackInfo;
        }

        for (BSDPackage pkg : m_lstPkgDeps) {
            strFBSDPackInfo += pkg.m_strPackageFileName + "; ";
        }
        strFBSDPackInfo += PackHoundParameters.getNewline();

        return strFBSDPackInfo;
    }
    /* <== Methods end. */

    public PkgVersion getPkgVersion() {
        String[] pkgFileNameComponents = m_strPackageFileName.split("-");
        int iComponentsCount = pkgFileNameComponents.length;
        if (iComponentsCount > 0) {
            PkgVersion ver = new PkgVersion();
            try {
                ver.init(pkgFileNameComponents[iComponentsCount - 1]);
                return ver;
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BSDPackage)) return false;

        BSDPackage that = (BSDPackage) o;

        if (m_strPackageFileName != null ? !m_strPackageFileName.equals(that.m_strPackageFileName) : that.m_strPackageFileName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return m_strPackageFileName != null ? m_strPackageFileName.hashCode() : 0;
    }
}
