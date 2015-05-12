package com.nlbhub.packhound;

import com.nlbhub.packhound.bsd.BSDPackInitializer;
import com.nlbhub.packhound.bsd.BSDPackNameResolver;
import com.nlbhub.packhound.config.PackHoundParameters;
import com.nlbhub.packhound.fbsd.FBSDPackInitializer;
import com.nlbhub.packhound.fbsd.FBSDPackNameResolver;
import com.nlbhub.packhound.obsd.OBSDPackInitializer;
import com.nlbhub.packhound.obsd.OBSDPackNameResolver;

/**
 * The EntityCreator class.
 *
 * @author Anton P. Kolosov
 * @version 1.0
 */
public class EntityCreator {
    private PackHoundParameters m_packHoundParameters;

    public EntityCreator(PackHoundParameters packHoundParameters) {
        m_packHoundParameters = packHoundParameters;
    }

    public BSDPackNameResolver newBSDPackNameResolver() throws Exception {
        switch (m_packHoundParameters.getOSType()) {
            case OS_FREEBSD:
                return new FBSDPackNameResolver();
            case OS_OPENBSD:
                return new OBSDPackNameResolver();
            default:
                throw new Exception("OS type is unknown");
        }
    }

    public BSDPackInitializer newBSDPackInitializer() throws Exception {
        switch (m_packHoundParameters.getOSType()) {
            case OS_FREEBSD:
                return new FBSDPackInitializer();
            case OS_OPENBSD:
                return new OBSDPackInitializer();
            default:
                throw new Exception("OS type is unknown");
        }
    }
}
