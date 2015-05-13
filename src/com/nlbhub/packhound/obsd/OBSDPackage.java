package com.nlbhub.packhound.obsd;

import com.nlbhub.packhound.bsd.BSDPackage;
import com.nlbhub.packhound.bsd.db.pkg.PkgEntry;
import com.nlbhub.packhound.obsd.db.pkg.OBSDPkgEntry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Antokolos on 12.05.15.
 */
public class OBSDPackage extends BSDPackage {
    private static final Pattern LINE_PATTERN = Pattern.compile("@depend (.*):(.*):(.*)");
    @Override
    protected PkgEntry newPkgEntry() {
        return new OBSDPkgEntry();
    }

    @Override
    protected BSDPackage getDependency(String curLine) {
        Matcher matcher = LINE_PATTERN.matcher(curLine);
        if (matcher.find()) {
            OBSDPackage pkgDepCur = new OBSDPackage();
            pkgDepCur.setStrPackageFileName(matcher.group(3) + ".tgz");
            return pkgDepCur;
        }
        return null;
    }
}
