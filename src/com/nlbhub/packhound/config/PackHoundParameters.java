/*
 * @(#)PackHoundParameters.java
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
package com.nlbhub.packhound.config;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The PackHoundParameters class.
 *
 * @author Anton P. Kolosov
 * @version 1.0
 */
public class PackHoundParameters {
    /* Static variables begin ==> */
    private static Logger LOG = (
        LoggerFactory.getLogger(PackHoundParameters.class)
    );
    private static final String PKGHOUND_PROPFILE = "pkghound.properties";
    private static final String PKG_STORAGE_DIR = "pkg.storage.dir";
    private static final String UNPACK_TEMP_DIR = "unpack.temp.dir";
    private static final String UNPACK_TEMP_FILE = "unpack.temp.file";
    private static final String PKG_DATABASE_DIR = "pkg.database.dir";
    private static final String OS_NAME = "os.name";
    private static final String INDEX_SRC_HOST = "index.src.host";
    private static final String PKG_SRC_HOST = "pkg.src.host";
    private static final String ALWAYS_RELOAD_INDEX = "always.reload.index";
    private static final String DOWNLOAD_ERROR_TIMEOUT = "download.error.timeout";
    private static final String USE_PROXY = "use.proxy";
    private static final String PROXY_HOST = "proxy.host";
    private static final String PROXY_PORT = "proxy.port";
    private static final String PROXY_USER = "proxy.user";
    private static final String PROXY_PASSWORD = "proxy.password";
    private static final String m_newline = (
        System.getProperty("line.separator")
    );
    /* <== Static variables end. */

    /* Instance variables begin ==> */
    private String m_pkgStorageDir;
    private String m_unpackTempDir;
    private String m_unpackTempFile;
    private String m_pkgDatabaseDir;
    private OSType m_OSType;
    private String m_indexSrcHost;
    private String m_pkgSrcHost;
    private boolean m_alwaysReloadIndex;
    private Proxy m_proxy;
    private int m_downloadErrorTimeout;
    /* <== Instance variables end. */

    /* Static blocks begin ==> */
    /* <== Static blocks end. */

    
    /* Constructors begin ==> */
    /**
     * Creating PackHoundParameters
     */
    public PackHoundParameters() {
        super();
        // TODO Auto-generated constructor stub
    }
    /* <== Constructors end. */
    /* Methods begin ==> */
    public void init() {
        System.out.println("Trying to load pkghound.properties...");
        Properties props = new Properties();
        try {
            props.load(
                getClass().getClassLoader().getResourceAsStream(
                    PKGHOUND_PROPFILE
                )
            );
            
            m_pkgStorageDir = props.getProperty(PKG_STORAGE_DIR);
            m_unpackTempDir = props.getProperty(UNPACK_TEMP_DIR);
            m_unpackTempFile = props.getProperty(UNPACK_TEMP_FILE);
            m_pkgDatabaseDir = props.getProperty(PKG_DATABASE_DIR);
            String osName = props.getProperty(OS_NAME);
            m_OSType = OSType.fromName(osName);
            if (m_OSType == OSType.UNKNOWN) {
                throw new Exception("OS type '" + osName + "' is unknown to me");
            }
            m_indexSrcHost = props.getProperty(INDEX_SRC_HOST);
            m_pkgSrcHost = props.getProperty(PKG_SRC_HOST);
            m_alwaysReloadIndex = (
                props.getProperty(ALWAYS_RELOAD_INDEX).toLowerCase()
                .equals("yes")
            );
            m_downloadErrorTimeout = (
                Integer.parseInt(props.getProperty(DOWNLOAD_ERROR_TIMEOUT))
            );
            final boolean useProxy = (
                props.getProperty(USE_PROXY).toLowerCase().equals("yes")
            );
            
            if (LOG.isDebugEnabled()) {
                LOG.debug(PKG_STORAGE_DIR + " = " + m_pkgStorageDir);
                LOG.debug(UNPACK_TEMP_DIR + " = " + m_unpackTempDir);
                LOG.debug(UNPACK_TEMP_FILE + " = " + m_unpackTempFile);
                LOG.debug(PKG_DATABASE_DIR + " = " + m_pkgDatabaseDir);
                LOG.debug(INDEX_SRC_HOST + " = " + m_indexSrcHost);
                LOG.debug(PKG_SRC_HOST + " = " + m_pkgSrcHost);
                LOG.debug("Use proxy: " + (useProxy ? "yes" : "no"));
                LOG.debug(
                    "Always reload index: "
                    + (m_alwaysReloadIndex ? "yes" : "no")
                );
            }
            
            if (useProxy) {
                final String proxyHost = props.getProperty(PROXY_HOST);
                final String proxyUser = props.getProperty(PROXY_USER);
                final String proxyPassword = props.getProperty(PROXY_PASSWORD);
                final int proxyPort = (
                    Integer.parseInt(props.getProperty(PROXY_PORT))
                );
                if (LOG.isDebugEnabled()) {
                    LOG.debug(PROXY_HOST + " = " + proxyHost);
                    LOG.debug(PROXY_USER + " = " + proxyUser);
                    LOG.debug(PROXY_PASSWORD + " = " + proxyPassword);
                    LOG.debug(PROXY_PORT + " = " + proxyPort);
                }
                
                SocketAddress addr = (
                    new InetSocketAddress(
                        proxyHost, proxyPort
                    )
                );
                m_proxy = new Proxy(Proxy.Type.HTTP, addr);
                if (!isBlank(proxyUser)) {
                    Authenticator.setDefault(new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(
                                    proxyUser,
                                    proxyPassword.toCharArray()
                            );
                        }
                    });
                }
            } else {
                m_proxy = null;
            }
        } catch (Exception e) {
            System.out.println(
                "pkghound.properties load failed: " + e.getMessage()
            );
        }
    }
    
    public String getPkgStorageDir() {
        return m_pkgStorageDir;
    }
    
    public String getUnpackTempDir() {
        return m_unpackTempDir;
    }
    
    public String getUnpackTempFile() {
        return m_unpackTempFile;
    }
    
    public String getPkgDatabaseDir() {
        return m_pkgDatabaseDir;
    }
    
    public boolean isAlwaysReloadIndex() {
        return m_alwaysReloadIndex;
    }
    
    public int getDownloadErrorTimeout() {
        return m_downloadErrorTimeout;
    }
    
    public void setDownloadErrorTimeout(int m_downloadErrorTimeout) {
        this.m_downloadErrorTimeout = m_downloadErrorTimeout;
    }
    
    public Proxy getProxy() {
        return m_proxy;
    }

    public OSType getOSType() {
        return m_OSType;
    }

    public String getIndexSrcHost() {
        return m_indexSrcHost;
    }

    public String getPkgSrcHost() {
        return m_pkgSrcHost;
    }
    
    public static String getNewline() {
        return m_newline;
    }

    private static boolean isBlank(String value) {
        return value == null || "".equals(value);
    }
    /* <== Methods end. */
}
