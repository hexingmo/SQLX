/*
 *    Copyright 2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.github.sqlx.util;

import lombok.extern.slf4j.Slf4j;
import org.joor.Reflect;

import java.io.Closeable;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * Utility class for JDBC operations.
 * @author He Xing Mo
 * @since 1.0
 */
@Slf4j
public class JdbcUtils {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private JdbcUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Tests the database connection using the provided driver class, URL, username, and password.
     *
     * @param driverClass The driver class for the database connection.
     * @param url         The JDBC URL for the database connection.
     * @param username    The username for the database connection.
     * @param password    The password for the database connection.
     * @return true if the connection test succeeds, false otherwise.
     */
    public static boolean testConnection(Class<? extends Driver> driverClass, String url, String username, String password) {
        if (driverClass == null) {
            return false;
        }
        boolean succeed;
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection(driverClass, url, username, password);
            statement = connection.createStatement();
            resultSet = statement.executeQuery("select 1");
            succeed = true;
        } catch (Exception e) {
            log.error("test connection error driverClass {} url {} username {}" ,driverClass.getName() , url , username ,e);
            succeed = false;
        } finally {
            close(connection);
            close(statement);
            close(resultSet);
        }
        return succeed;
    }

    /**
     * Establishes a database connection using the provided driver class, URL, username, and password.
     *
     * @param driverClass The driver class for the database connection.
     * @param url         The JDBC URL for the database connection.
     * @param username    The username for the database connection.
     * @param password    The password for the database connection.
     * @return A new database connection.
     * @throws SQLException If a database access error occurs or this method is called on a closed connection.
     */
    public static Connection getConnection(Class<? extends Driver> driverClass, String url, String username, String password) throws SQLException {
        Driver driver = Reflect.onClass(driverClass).create().get();
        DriverManager.registerDriver(driver);
        return DriverManager.getConnection(url, username, password);
    }


    /**
     * Determines the type of database based on the provided JDBC URL.
     *
     * @param rawUrl The JDBC URL for the database connection.
     * @return The type of the database, or null if it cannot be determined.
     */
    public static String getDbType(String rawUrl) {
        if (rawUrl == null) {
            return null;
        }

        if (rawUrl.startsWith("jdbc:derby:") || rawUrl.startsWith("jdbc:log4jdbc:derby:")) {
            return "derby";
        } else if (rawUrl.startsWith("jdbc:mysql:") || rawUrl.startsWith("jdbc:cobar:")
                || rawUrl.startsWith("jdbc:log4jdbc:mysql:")) {
            return "mysql";
        } else if (rawUrl.startsWith("jdbc:mariadb:")) {
            return "mariadb";
        } else if (rawUrl.startsWith("jdbc:oracle:") || rawUrl.startsWith("jdbc:log4jdbc:oracle:")) {
            return "oracle";
        } else if (rawUrl.startsWith("jdbc:alibaba:oracle:")) {
            return "AliOracle";
        } else if (rawUrl.startsWith("jdbc:oceanbase:")) {
            return "oceanbase";
        } else if (rawUrl.startsWith("jdbc:oceanbase:oracle:")) {
            return "oceanbase_oracle";
        } else if (rawUrl.startsWith("jdbc:microsoft:") || rawUrl.startsWith("jdbc:log4jdbc:microsoft:")) {
            return "sqlserver";
        } else if (rawUrl.startsWith("jdbc:sqlserver:") || rawUrl.startsWith("jdbc:log4jdbc:sqlserver:")) {
            return "sqlserver";
        } else if (rawUrl.startsWith("jdbc:sybase:Tds:") || rawUrl.startsWith("jdbc:log4jdbc:sybase:")) {
            return "sybase";
        } else if (rawUrl.startsWith("jdbc:jtds:") || rawUrl.startsWith("jdbc:log4jdbc:jtds:")) {
            return "jtds";
        } else if (rawUrl.startsWith("jdbc:fake:") || rawUrl.startsWith("jdbc:mock:")) {
            return "mock";
        } else if (rawUrl.startsWith("jdbc:postgresql:") || rawUrl.startsWith("jdbc:log4jdbc:postgresql:")) {
            return "postgresql";
        } else if (rawUrl.startsWith("jdbc:edb:")) {
            return "edb";
        } else if (rawUrl.startsWith("jdbc:hsqldb:") || rawUrl.startsWith("jdbc:log4jdbc:hsqldb:")) {
            return "hsql";
        } else if (rawUrl.startsWith("jdbc:odps:")) {
            return "odps";
        } else if (rawUrl.startsWith("jdbc:db2:")) {
            return "db2";
        } else if (rawUrl.startsWith("jdbc:sqlite:")) {
            return "sqlite";
        } else if (rawUrl.startsWith("jdbc:ingres:")) {
            return "ingres";
        } else if (rawUrl.startsWith("jdbc:h2:") || rawUrl.startsWith("jdbc:log4jdbc:h2:")) {
            return "h2";
        } else if (rawUrl.startsWith("jdbc:mckoi:")) {
            return "mckoi";
        } else if (rawUrl.startsWith("jdbc:cloudscape:")) {
            return "cloudscape";
        } else if (rawUrl.startsWith("jdbc:informix-sqli:") || rawUrl.startsWith("jdbc:log4jdbc:informix-sqli:")) {
            return "informix";
        } else if (rawUrl.startsWith("jdbc:timesten:")) {
            return "timesten";
        } else if (rawUrl.startsWith("jdbc:as400:")) {
            return "as400";
        } else if (rawUrl.startsWith("jdbc:sapdb:")) {
            return "sapdb";
        } else if (rawUrl.startsWith("jdbc:JSQLConnect:")) {
            return "JSQLConnect";
        } else if (rawUrl.startsWith("jdbc:JTurbo:")) {
            return "JTurbo";
        } else if (rawUrl.startsWith("jdbc:firebirdsql:")) {
            return "firebirdsql";
        } else if (rawUrl.startsWith("jdbc:interbase:")) {
            return "interbase";
        } else if (rawUrl.startsWith("jdbc:pointbase:")) {
            return "pointbase";
        } else if (rawUrl.startsWith("jdbc:edbc:")) {
            return "edbc";
        } else if (rawUrl.startsWith("jdbc:mimer:multi1:")) {
            return "mimer";
        } else if (rawUrl.startsWith("jdbc:dm:")) {
            return "dm";
        } else if (rawUrl.startsWith("jdbc:kingbase:")) {
            return "kingbase";
        } else if (rawUrl.startsWith("jdbc:gbase:")) {
            return "gbase";
        } else if (rawUrl.startsWith("jdbc:xugu:")) {
            return "xugu";
        } else if (rawUrl.startsWith("jdbc:log4jdbc:")) {
            return "log4jdbc";
        } else if (rawUrl.startsWith("jdbc:hive:")) {
            return "hive";
        } else if (rawUrl.startsWith("jdbc:hive2:")) {
            return "hive";
        } else if (rawUrl.startsWith("jdbc:phoenix:")) {
            return "phoenix";
        } else if (rawUrl.startsWith("jdbc:elastic:")) {
            return "elastic_search";
        } else if (rawUrl.startsWith("jdbc:clickhouse:")) {
            return "clickhouse";
        } else if (rawUrl.startsWith("jdbc:presto:")) {
            return "presto";
        } else if (rawUrl.startsWith("jdbc:inspur:")) {
            return "kdb";
        } else if (rawUrl.startsWith("jdbc:polardb")) {
            return "polardb";
        } else {
            return null;
        }
    }

    /**
     * Returns the SQL type name corresponding to the given SQL type code.
     *
     * @param sqlType The SQL type code.
     * @return The SQL type name.
     */
    public static String getTypeName(int sqlType) {
        switch (sqlType) {
            case Types.ARRAY:
                return "ARRAY";

            case Types.BIGINT:
                return "BIGINT";

            case Types.BINARY:
                return "BINARY";

            case Types.BIT:
                return "BIT";

            case Types.BLOB:
                return "BLOB";

            case Types.BOOLEAN:
                return "BOOLEAN";

            case Types.CHAR:
                return "CHAR";

            case Types.CLOB:
                return "CLOB";

            case Types.DATALINK:
                return "DATALINK";

            case Types.DATE:
                return "DATE";

            case Types.DECIMAL:
                return "DECIMAL";

            case Types.DISTINCT:
                return "DISTINCT";

            case Types.DOUBLE:
                return "DOUBLE";

            case Types.FLOAT:
                return "FLOAT";

            case Types.INTEGER:
                return "INTEGER";

            case Types.JAVA_OBJECT:
                return "JAVA_OBJECT";

            case Types.LONGNVARCHAR:
                return "LONGNVARCHAR";

            case Types.LONGVARBINARY:
                return "LONGVARBINARY";

            case Types.NCHAR:
                return "NCHAR";

            case Types.NCLOB:
                return "NCLOB";

            case Types.NULL:
                return "NULL";

            case Types.NUMERIC:
                return "NUMERIC";

            case Types.NVARCHAR:
                return "NVARCHAR";

            case Types.REAL:
                return "REAL";

            case Types.REF:
                return "REF";

            case Types.ROWID:
                return "ROWID";

            case Types.SMALLINT:
                return "SMALLINT";

            case Types.SQLXML:
                return "SQLXML";

            case Types.STRUCT:
                return "STRUCT";

            case Types.TIME:
                return "TIME";

            case Types.TIMESTAMP:
                return "TIMESTAMP";

            case Types.TINYINT:
                return "TINYINT";

            case Types.VARBINARY:
                return "VARBINARY";

            case Types.VARCHAR:
                return "VARCHAR";

            default:
                return "OTHER";

        }
    }

    /**
     * Extracts the database name from the provided JDBC URL.
     *
     * @param url The JDBC URL for the database connection.
     * @return The name of the database, or null if it cannot be extracted.
     */
    public static String getDatabaseName(String url) {

        if (url == null || url.trim().isEmpty()) {
            return null;
        }

        try {
            // MySQL: jdbc:mysql://hostname:port/database
            if (url.contains("mysql")) {
                return extractDatabaseName(url, "mysql");
            }

            // PostgreSQL : jdbc:postgresql://hostname:port/database
            if (url.contains("postgresql")) {
                return extractDatabaseName(url, "postgresql");
            }

            // Oracle :
            // - jdbc:oracle:thin:@hostname:port:database
            // - jdbc:oracle:thin:@hostname:port/service_name
            if (url.contains("oracle")) {
                int index = url.lastIndexOf(':');
                if (index == -1) {
                    index = url.lastIndexOf('/');
                }
                return index > 0 ? url.substring(index + 1) : null;
            }

            // SQL Server : jdbc:sqlserver://hostname:port;databaseName=database
            if (url.contains("sqlserver")) {
                String[] parts = url.split(";");
                for (String part : parts) {
                    if (part.startsWith("databaseName=")) {
                        return part.substring("databaseName=".length());
                    }
                }
            }

            // H2 :
            // jdbc:h2:~/test or jdbc:h2:file:/data/sample
            // jdbc:h2:mem:test
            // jdbc:h2:tcp://localhost:9092/~/test
            if (url.contains("h2:")) {
                return extractH2DatabaseName(url);
            }

            return extractDatabaseName(url, null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extracts the H2 database name from the provided JDBC URL.
     *
     * @param url The JDBC URL for the H2 database connection.
     * @return The name of the H2 database, or null if it cannot be extracted.
     */
    private static String extractH2DatabaseName(String url) {
        try {
            String cleanUrl = url.substring(url.indexOf("h2:") + 3);

            int paramIndex = cleanUrl.indexOf(';');
            if (paramIndex > 0) {
                cleanUrl = cleanUrl.substring(0, paramIndex);
            }

            if (cleanUrl.startsWith("mem:")) {
                cleanUrl = cleanUrl.substring(4);
            }

            if (cleanUrl.startsWith("tcp:")) {
                int lastSlash = cleanUrl.lastIndexOf('/');
                if (lastSlash > 0) {
                    cleanUrl = cleanUrl.substring(lastSlash + 1);
                }
            }

            if (cleanUrl.startsWith("file:")) {
                cleanUrl = cleanUrl.substring(5);
            }

            if (cleanUrl.startsWith("~/")) {
                cleanUrl = cleanUrl.substring(2);
            }

            int lastSlash = cleanUrl.lastIndexOf('/');
            if (lastSlash > 0) {
                cleanUrl = cleanUrl.substring(lastSlash + 1);
            }

            int lastBackslash = cleanUrl.lastIndexOf('\\');
            if (lastBackslash > 0) {
                cleanUrl = cleanUrl.substring(lastBackslash + 1);
            }

            return cleanUrl;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extracts the database name from the provided JDBC URL based on the database type.
     *
     * @param url     The JDBC URL for the database connection.
     * @param dbType  The type of the database.
     * @return The name of the database, or null if it cannot be extracted.
     */
    private static String extractDatabaseName(String url, String dbType) {
        String cleanUrl = url;

        int queryIndex = cleanUrl.indexOf('?');
        if (queryIndex > 0) {
            cleanUrl = cleanUrl.substring(0, queryIndex);
        }

        if (dbType != null) {
            int typeIndex = cleanUrl.indexOf(dbType);
            if (typeIndex > 0) {
                cleanUrl = cleanUrl.substring(typeIndex + dbType.length());
            }
        }

        cleanUrl = cleanUrl.replaceAll("^jdbc:", "")
                .replaceAll("^//", "")
                .replaceAll("^:", "");

        String[] parts = cleanUrl.split("/");
        for (int i = parts.length - 1; i >= 0; i--) {
            String part = parts[i].trim();
            if (!part.isEmpty()) {
                return part;
            }
        }

        return null;
    }

    /**
     * Closes the provided database connection.
     *
     * @param x The database connection to close.
     */
    public static void close(Connection x) {
        if (x == null) {
            return;
        }

        try {
            if (x.isClosed()) {
                return;
            }

            x.close();
        } catch (Exception e) {
            log.debug("close connection error", e);
        }
    }

    /**
     * Closes the provided SQL statement.
     *
     * @param x The SQL statement to close.
     */
    public static void close(Statement x) {
        if (x == null) {
            return;
        }
        try {
            x.close();
        } catch (Exception e) {
            log.debug("close statement error", e);
        }
    }

    /**
     * Closes the provided result set.
     *
     * @param x The result set to close.
     */
    public static void close(ResultSet x) {
        if (x == null) {
            return;
        }
        try {
            x.close();
        } catch (Exception e) {
            log.debug("close result set error", e);
        }
    }


    /**
     * Closes the provided closable resource.
     *
     * @param x The closable resource to close.
     */
    public static void close(Closeable x) {
        if (x == null) {
            return;
        }

        try {
            x.close();
        } catch (Exception e) {
            log.debug("close error", e);
        }
    }

    /**
     * Frees the provided BLOB resource.
     *
     * @param x The BLOB resource to free.
     */
    public static void close(Blob x) {
        if (x == null) {
            return;
        }

        try {
            x.free();
        } catch (Exception e) {
            log.debug("close error", e);
        }
    }

    public static void close(Clob x) {
        if (x == null) {
            return;
        }

        try {
            x.free();
        } catch (Exception e) {
            log.debug("close error", e);
        }
    }
}
