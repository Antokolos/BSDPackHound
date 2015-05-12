/*
 * @(#)DatabaseProcessor.java
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
package com.nlbhub.packhound.fbsd.db;

import java.sql.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nlbhub.jdbc.AccessJDBCUtil;


/**
 * The DatabaseProcessor class manages requests to the database. 
 *
 * @author Anton P. Kolosov
 * @version 1.0
 */
public class DatabaseProcessor {
    /* Static variables begin ==> */
    
    /**
     * Logger for DatabaseProcessor class
     */
    private static Logger LOG = (
        LoggerFactory.getLogger(DatabaseProcessor.class)
    );
    /* <== Static variables end. */
    
    /* Instance variables begin ==> */
    
    /**
     * Query string to execute (with the aid of PreparedStatement)
     */
    private String m_strQueryFBSDDepsTemplate  = (
        "SELECT FBSD_PKGS.PKG_NAME "
            + "FROM FBSD_PKGS, FBSD_PKG_DEPS "
            + "WHERE FBSD_PKG_DEPS.DEP_GRP_CODE IN "
                + "(SELECT FBSD_PKGS.DEP_GRP_CODE "
                    + "FROM FBSD_PKGS"
                    + "WHERE FBSD_PKGS.PKG_NAME = ?)"
    );
    
    private String m_strQueryAddFBSDPackageTemplate = (
        "INSERT INTO FBSD_PKGS(DEP_GRP_CODE,"
            + "PKG_NAME)" +
        "VALUES(NULL,"
            + "?)"
         
    );
    
    /**
     * Connection object
     */
    private java.sql.Connection m_conn = null;
    
    /* <== Instance variables end. */
    
    /* Constructors begin ==> */
    
    /**
     * Creating DatabaseProcessor
     * @param strDatabasePath Full path to the database file
     */
    public DatabaseProcessor(String strDatabasePath) {
        super();
        try {
            m_conn = AccessJDBCUtil.getAccessDBConnection(strDatabasePath);
            if (m_conn != null) {
                LOG.info(
                    "Database " + strDatabasePath + " connected successfully!"
                );
            }
                
        } catch (SQLException s) {
            // TODO Auto-generated catch block
            LOG.info(s.getMessage());
        }
    }
    
    /* <== Constructors end. */
    
    /* Methods begin ==> */
    
    /**
     * Disconnect from the current database
     * @return true if disconnected successfully, false otherwise
     */
    public Boolean disconnect() {
        if (m_conn != null) {
            try {
                m_conn.close();
                LOG.info("Database disconnected.");
            } catch (SQLException s) {
                LOG.info(s.getMessage());
            }
            return true;
        } else {
            LOG.info("Nothing to disconnect.");
            return false;
        }
    }
    
    /**
     * Prints dependencies of selected package
     * @param strPkgName package name
     */
    public void printPkgDeps(String strPkgName) {
        PreparedStatement pstmt;
        
        try {
            pstmt = m_conn.prepareStatement(m_strQueryFBSDDepsTemplate);
            pstmt.setString(1, strPkgName);
            if (pstmt.execute()) {
                ResultSet rs = pstmt.getResultSet();
                if (rs != null) {
                    while (rs.next()) {
                        System.out.println(rs.getString(1));
                    }
                }
            } else {
                LOG.info("DDL executed successfully");
            }
        } catch (SQLException s) {
            // TODO Auto-generated catch block
            LOG.info(s.getMessage());
        }
    }
    
    /**
     * Adds new package to FBSD_PKGS table, with initial setting of
     * DEP_GRP_CODE == NULL. Can be changed later with addPkgDep
     * @param strPkgName package name
     */
    public void addPackage(String strPkgName) {
        PreparedStatement pstmt;
        
        try {
            pstmt = m_conn.prepareStatement(m_strQueryAddFBSDPackageTemplate);
            pstmt.setString(1, strPkgName);
            if (pstmt.executeUpdate() == 1) {
                System.out.println(strPkgName + " added successfully");
            } else {
                LOG.info("DDL executed successfully");
            }
        } catch (SQLException s) {
            // TODO Auto-generated catch block
            LOG.info(s.getMessage());
        }
    }
    /* <== Methods end. */
}
