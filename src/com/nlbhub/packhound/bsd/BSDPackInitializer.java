/*
 * @(#)BSDPackInitializer.java
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

import java.util.ArrayList;
import java.util.LinkedList;

import com.nlbhub.packhound.config.PackHoundParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The BSDPackInitializer class.
 *
 * @author Anton P. Kolosov
 * @version 1.0
 */
public abstract class BSDPackInitializer {
    /* Static variables begin ==> */
    private static Logger LOG = (
            LoggerFactory.getLogger(BSDPackInitializer.class)
    );
    /* <== Static variables end. */

    /* Instance variables begin ==> */
    private LinkedList<BSDPackage> m_lstRequiredPackages;
    /* <== Instance variables end. */

    /* Static blocks begin ==> */
    /* <== Static blocks end. */

    /* Constructors begin ==> */
    /**
     * Creating FBSDPackInitializer
     */
    protected BSDPackInitializer() {
        super();
        m_lstRequiredPackages = new LinkedList<BSDPackage>();
    }

    /* <== Constructors end. */
    /* Methods begin ==> */
    public void InitFBSDPacks(
            String basePkgName, final PackHoundParameters pkgHP
    ) {
        BSDPackage pkg = newPackage();
        pkg.init(basePkgName, null, pkgHP);
        InitBSDPacks(pkg, pkgHP);
    }

    protected abstract BSDPackage newPackage();

    public void InitBSDPacks(
            BSDPackage basePkg, final PackHoundParameters pkgHP
    ) {
        ArrayList<BSDPackage> lstDeps = basePkg.getPkgDeps();

        if (lstDeps.isEmpty()) {
            m_lstRequiredPackages.addFirst(basePkg);
            
            /*DatabaseProcessor dp = (
                new DatabaseProcessor("./../db/FBSD_PACKAGES.mdb")
            );
            dp.addPackage(basePkg.getPackageFileName());*/
        } else {
            m_lstRequiredPackages.addLast(basePkg);
        }

        for (BSDPackage pkgCur : lstDeps) {
            if (
                    !m_lstRequiredPackages
                            .contains(pkgCur)
                    ) {
                pkgCur.init(
                        pkgCur.getPackageFileName(),
                        basePkg.getPackageFileName(),
                        pkgHP
                );
                InitBSDPacks(pkgCur, pkgHP);
            }
        }
    }

    /**
     * Prints required packages and its dependencies.
     */
    public void printRequiredPackages() {
        StringBuilder sb = new StringBuilder();
        sb.append("Required packages: ");
        sb.append(PackHoundParameters.getNewline());
        for (BSDPackage pkgCur : m_lstRequiredPackages) {
            sb.append(pkgCur.toString());
        }
        LOG.info(sb.toString());
    }
    /* <== Methods end. */
}