/*
 * @(#)AccessJDBCUtil.java
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
package com.nlbhub.jdbc;

import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The AccessJDBCUtil class provides connection to the MS Access database. 
 *
 * @author Anton P. Kolosov
 * @version 1.0
 */
public class AccessJDBCUtil {
    /* Static variables begin ==> */
    
    /**
     * Logger for AccessJDBCUtil class
     */
    private static Logger LOG = LoggerFactory.getLogger(AccessJDBCUtil.class);
    
    /**
     * Prefix of the database URL for JDBC call
     */
    private static final String ACCESS_DBURL_PREFIX = (
        "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ="
    );
    
    /**
     * Suffix of the database URL for JDBC call
     */
    private static final String ACCESS_DBURL_SUFFIX = (
        ";DriverID=22;READONLY=true}"
    );
    
    /* <== Static variables end. */
    
    /* Static blocks begin ==> */
    
    static {
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
        } catch (ClassNotFoundException e) {
            LOG.info("JdbcOdbc Bridge Driver not found!");
            // ABORT ABORT... How? System.exit(1) is not nice from webapp...
        }
    }
    
    /* <== Static blocks end. */

    /* Constructors begin ==> */
    
    /**
     * Creating AccessJDBCUtil
     */
    protected AccessJDBCUtil() {
        super();
    }
    
    /* <== Constructors end. */

    /* Methods begin ==> */
    
    /**
     * Creates a Connection to a Access Database
     * @param filename Full filename of the Access database with path
     * @return Connection object for established connection
     * @throws SQLException Some exception in a case of error
     */
    public static java.sql.Connection getAccessDBConnection(String filename)
    throws SQLException {
        filename = filename.replace('\\', '/').trim();
        String databaseURL = (
            ACCESS_DBURL_PREFIX + filename + ACCESS_DBURL_SUFFIX
        );
        LOG.info("Database URL: " + databaseURL);
        return DriverManager.getConnection(databaseURL, "", "");
    }
    
    /* <== Methods end. */
}
