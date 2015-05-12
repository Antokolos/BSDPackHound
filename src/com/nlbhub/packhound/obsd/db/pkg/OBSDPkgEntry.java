package com.nlbhub.packhound.obsd.db.pkg;

import com.nlbhub.packhound.arch.UnTbz;
import com.nlbhub.packhound.bsd.db.pkg.PkgEntry;
import com.nlbhub.packhound.config.PackHoundParameters;

/**
 * Created by Antokolos on 12.05.15.
 */
public class OBSDPkgEntry extends PkgEntry {
    @Override
    public boolean unpackPackageContents(PackHoundParameters phParms, String zipFName) {
        return UnTbz.unTgzOBSDPackageContents(phParms, zipFName);
    }
}
