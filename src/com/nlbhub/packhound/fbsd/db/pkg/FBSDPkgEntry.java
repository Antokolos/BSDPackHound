package com.nlbhub.packhound.fbsd.db.pkg;

import com.nlbhub.packhound.util.Unarchiver;
import com.nlbhub.packhound.bsd.db.pkg.PkgEntry;
import com.nlbhub.packhound.config.PackHoundParameters;

/**
 * Created by apkolosov on 5/12/15.
 */
public class FBSDPkgEntry extends PkgEntry {
    @Override
    public boolean unpackPackageContents(PackHoundParameters phParms, String zipFName) {
        return Unarchiver.untbz(phParms, zipFName);
    }
}
