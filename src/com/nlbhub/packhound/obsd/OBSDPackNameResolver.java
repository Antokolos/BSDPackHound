package com.nlbhub.packhound.obsd;

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
 * Created by Antokolos on 12.05.15.
 */
public class OBSDPackNameResolver implements BSDPackNameResolver {
    private static Logger LOG = LoggerFactory.getLogger(OBSDPackNameResolver.class);

    @Override
    public void retrieveIndex(boolean forceIndexReload, PackHoundParameters phParms) {
        LOG.info("Checking INDEX file...");
        File indexFile = new File(phParms.getUnpackTempDir() + "/INDEX");
        if (!forceIndexReload && indexFile.exists()) {
            LOG.info("OK");
        } else {
            LOG.info("INDEX file not found!");
            LOG.info("Downloading INDEX file...");

            File fDest = new File(phParms.getPkgStorageDir(), "INDEX");
            try {
                FileUpload.download(
                        phParms.getProxy(),
                        phParms.getIndexSrcHost(),
                        null,
                        null,
                        "/INDEX", fDest,
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

    @Override
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
                            return (fullPkgName + ".tgz");
                        }
                    } else {
                        int idx2 = fullPkgName.lastIndexOf("-");
                        if (idx2 > 0) {
                            String pkgCur = strCurLine.substring(0, idx2);
                            if (pkgCur.equals(strToSearch)) {
                                return fullPkgName + ".tgz";
                            }
                        }
                    }
                }
            }
        } while (strCurLine != null);
        return null;
    }
}
