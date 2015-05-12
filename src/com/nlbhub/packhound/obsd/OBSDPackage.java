package com.nlbhub.packhound.obsd;

import com.nlbhub.packhound.bsd.BSDPackage;
import com.nlbhub.packhound.bsd.db.pkg.PkgEntry;
import com.nlbhub.packhound.obsd.db.pkg.OBSDPkgEntry;

/**
 * Created by Antokolos on 12.05.15.
 */
public class OBSDPackage extends BSDPackage {
    @Override
    protected PkgEntry newPkgEntry() {
        return new OBSDPkgEntry();
    }

    @Override
    protected BSDPackage getDependency(String curLine) {
        if (curLine.startsWith("@dependency")) {
            OBSDPackage pkgDepCur = new OBSDPackage();
            pkgDepCur.setStrPackageFileName(curLine.substring(8) + ".tgz");
            return pkgDepCur;
        }
        return null;
    }
}
