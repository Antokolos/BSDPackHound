package com.nlbhub.packhound.obsd;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Antokolos on 12.05.15.
 */
public class OBSDPackNameResolver implements BSDPackNameResolver {
    private static final Pattern PKG_FILE_NAME_PATTERN = Pattern.compile("\\s([^\\s]*\\.tgz)$");
    private static Logger LOG = LoggerFactory.getLogger(OBSDPackNameResolver.class);

    @Override
    public void retrieveIndex(boolean forceIndexReload, PackHoundParameters phParms) {
        /*
         * We can get INDEX file from ports.tar.gz archive. But since we need only filenames, we can use index.txt
         */
        LOG.info("Checking index.txt file...");
        File indexFile = new File(phParms.getPkgStorageDir() + "/index.txt");
        if (!forceIndexReload && indexFile.exists()) {
            LOG.info("OK");
        } else {
            LOG.info("index.txt file not found!");
            LOG.info("Downloading index.txt file...");

            File fDest = new File(phParms.getPkgStorageDir(), "index.txt");
            try {
                FileUpload.download(
                        phParms.getProxy(),
                        phParms.getIndexSrcHost(),
                        null,
                        null,
                        "/index.txt", fDest,
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
                new FileReader(new File(phParms.getPkgStorageDir(), "index.txt"))
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
                Matcher matcher = PKG_FILE_NAME_PATTERN.matcher(strCurLine);
                if (matcher.find()) {
                    String fullPkgName = matcher.group(1);
                    if (searchLike) {
                        if (fullPkgName.matches(strToSearch)) {
                            return fullPkgName;
                        }
                    } else {
                        int idx2 = fullPkgName.lastIndexOf("-");
                        if (idx2 > 0) {
                            String pkgCur = fullPkgName.substring(0, idx2);
                            if (pkgCur.equalsIgnoreCase(strToSearch)) {
                                return fullPkgName;
                            }
                        }
                    }
                }
            }
        } while (strCurLine != null);
        return null;
    }
}
