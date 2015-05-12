package com.nlbhub.packhound.obsd;

import com.nlbhub.packhound.bsd.BSDPackInitializer;
import com.nlbhub.packhound.bsd.BSDPackage;

/**
 * Created by Antokolos on 12.05.15.
 */
public class OBSDPackInitializer extends BSDPackInitializer {
    @Override
    protected BSDPackage newPackage() {
        return new OBSDPackage();
    }
}
