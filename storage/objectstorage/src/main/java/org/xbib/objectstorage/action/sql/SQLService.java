/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public
 * License, these Appropriate Legal Notices must retain the display of the
 * "Powered by xbib" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.objectstorage.action.sql;

import org.xbib.date.DateUtil;
import org.xbib.objectstorage.adapter.AbstractAdapter;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The SQL service class manages the SQL access to the JDBC connection.
 */
public class SQLService {

    private final static Logger logger = Logger.getLogger(SQLService.class.getName());
    private final static Map<AbstractAdapter, SQLService> instances = new HashMap();
    private Connection connection;
    private int rounding;
    private int scale = -1;

    private SQLService() {
    }

    public static SQLService getInstance(AbstractAdapter adapter) throws IOException {
        if (!instances.containsKey(adapter)) {
            SQLService service = new SQLService();
            try {
                Connection connection = service.getConnection(adapter.getDriverClassName(),
                        adapter.getConnectionSpec(), adapter.getUser(), adapter.getPassword());
                service.setConnection(connection);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
                throw new IOException(ex);
            }
            instances.put(adapter, service);
        }
        return instances.get(adapter);
    }

    public static void shutdown() {
        for (SQLService service : instances.values()) {
            try {
                service.getConnection().close();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        instances.clear();
    }

    public SQLService setConnection(Connection connection) {
        this.connection = connection;
        return this;
    }

    public Connection getConnection() {
        return connection;
    }

    private Connection getConnection(final String user, final String pass)
            throws SQLException, UnknownHostException, NameNotFoundException, NamingException {
        String name = "jdbc/" + InetAddress.getLocalHost().getHostName();
        Context context = new InitialContext();
        Object o = context.lookup(name);
        if (o instanceof DataSource) {
            DataSource ds = (DataSource) o;
            return ds.getConnection(user, pass);
        } else {
            return null;
        }
    }

    /**
     * Get JDBC connection
     *
     * @param driverClassName
     * @param url
     * @param user
     * @param password
     * @return the connection
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private Connection getConnection(final String driverClassName,
                                     final String url, final String user, final String password)
            throws ClassNotFoundException, SQLException, UnknownHostException,
            NameNotFoundException, NamingException {
        Connection c = getConnection(user, password);
        if (c != null) {
            return c;
        }
        Class.forName(driverClassName);
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Prepare statement
     *
     * @param connection
     * @param sql
     * @return a prepared statement
     * @throws SQLException
     */
    public PreparedStatement prepareStatement(Connection connection, String sql)
            throws SQLException {
        return connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    /**
     * Bind values to prepared statement
     *
     * @param pstmt
     * @param values
     * @throws SQLException
     */
    public PreparedStatement bind(PreparedStatement pstmt, String[] keys, Map<String, Object> values) throws SQLException {
        if (keys == null || values == null) {
            return pstmt;
        }
        for (int i = 0; i < keys.length; i++) {
            bind(pstmt, i + 1, values.get(keys[i]));
        }
        return pstmt;
    }

    /**
     * Execute prepared qquery statement
     *
     * @param statement
     * @return the result set
     * @throws SQLException
     */
    public ResultSet executeQuery(PreparedStatement statement) throws SQLException {
        return statement.executeQuery();
    }

    /**
     * Execute prepared insert/update/delete statement
     *
     * @param statement
     * @return the result set
     * @throws SQLException
     */
    public int executeUpdate(PreparedStatement statement) throws SQLException {
        int n = statement.executeUpdate();
        if (!connection.getAutoCommit()) {
            connection.commit();
        }
        return n;
    }

    /**
     * Execute statement
     *
     * @param statement
     * @return the result set
     * @throws SQLException
     */
    public boolean execute(PreparedStatement statement) throws SQLException {
        boolean b =  statement.execute();
        if (!connection.getAutoCommit()) {
            connection.commit();
        }
        return b;
    }

    /**
     * Get next row and prepare the values for processing. The labels of each
     * columns are used for the RowListener as paths for JSON object merging.
     *
     * @param result   the result set
     * @return true if row exists and was processed, false otherwise
     * @throws SQLException
     * @throws IOException
     */
    public boolean nextRow(ResultSet result)
            throws SQLException, IOException {
        if (result.next()) {
            processRow(result);
            return true;
        }
        return false;
    }

    private void processRow(ResultSet result)
            throws SQLException, IOException {
        LinkedList<String> keys = new LinkedList();
        LinkedList<Object> values = new LinkedList();
        ResultSetMetaData metadata = result.getMetaData();
        int columns = metadata.getColumnCount();
        for (int i = 1; i <= columns; i++) {
            String name = metadata.getColumnLabel(i);
            keys.add(name);
            switch (metadata.getColumnType(i)) {
                /**
                 * The JDBC types CHAR, VARCHAR, and LONGVARCHAR are closely
                 * related. CHAR represents a small, fixed-length character
                 * string, VARCHAR represents a small, variable-length character
                 * string, and LONGVARCHAR represents a large, variable-length
                 * character string.
                 */
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR: {
                    String s = result.getString(i);
                    values.add(s);
                    break;
                }
                case Types.NCHAR:
                case Types.NVARCHAR:
                case Types.LONGNVARCHAR: {
                    String s = result.getNString(i);
                    values.add(s);
                    break;
                }
                /**
                 * The JDBC types BINARY, VARBINARY, and LONGVARBINARY are
                 * closely related. BINARY represents a small, fixed-length
                 * binary value, VARBINARY represents a small, variable-length
                 * binary value, and LONGVARBINARY represents a large,
                 * variable-length binary value
                 */
                case Types.BINARY:
                case Types.VARBINARY:
                case Types.LONGVARBINARY: {
                    byte[] b = result.getBytes(name);
                    values.add(b);
                    break;
                }
                /**
                 * The JDBC type ARRAY represents the SQL3 type ARRAY.
                 *
                 * An ARRAY value is mapped to an instance of the Array
                 * interface in the Java programming language. If a driver
                 * follows the standard implementation, an Array object
                 * logically points to an ARRAY value on the server rather than
                 * containing the elements of the ARRAY object, which can
                 * greatly increase efficiency. The Array interface contains
                 * methods for materializing the elements of the ARRAY object on
                 * the client in the form of either an array or a ResultSet
                 * object.
                 */
                case Types.ARRAY: {
                    Array a = result.getArray(i);
                    values.add(a != null ? a.toString() : null);
                    break;
                }
                /**
                 * The JDBC type BIGINT represents a 64-bit signed integer value
                 * between -9223372036854775808 and 9223372036854775807.
                 *
                 * The corresponding SQL type BIGINT is a nonstandard extension
                 * to SQL. In practice the SQL BIGINT type is not yet currently
                 * implemented by any of the major databases, and we recommend
                 * that its use be avoided in code that is intended to be
                 * portable.
                 *
                 * The recommended Java mapping for the BIGINT type is as a Java
                 * long.
                 */
                case Types.BIGINT: {
                    long l = result.getLong(i);
                    values.add(l);
                    break;
                }
                /**
                 * The JDBC type BIT represents a single bit value that can be
                 * zero or one.
                 *
                 * SQL-92 defines an SQL BIT type. However, unlike the JDBC BIT
                 * type, this SQL-92 BIT type can be used as a parameterized
                 * type to define a fixed-length binary string. Fortunately,
                 * SQL-92 also permits the use of the simple non-parameterized
                 * BIT type to represent a single binary digit, and this usage
                 * corresponds to the JDBC BIT type. Unfortunately, the SQL-92
                 * BIT type is only required in "full" SQL-92 and is currently
                 * supported by only a subset of the major databases. Portable
                 * code may therefore prefer to use the JDBC SMALLINT type,
                 * which is widely supported.
                 */
                case Types.BIT: {
                    int n = result.getInt(i);
                    values.add(n);
                    break;
                }
                /**
                 * The JDBC type BOOLEAN, which is new in the JDBC 3.0 API, maps
                 * to a boolean in the Java programming language. It provides a
                 * representation of true and false, and therefore is a better
                 * match than the JDBC type BIT, which is either 1 or 0.
                 */
                case Types.BOOLEAN: {
                    boolean b = result.getBoolean(i);
                    values.add(b);
                    break;
                }
                /**
                 * The JDBC type BLOB represents an SQL3 BLOB (Binary Large
                 * Object).
                 *
                 * A JDBC BLOB value is mapped to an instance of the Blob
                 * interface in the Java programming language. If a driver
                 * follows the standard implementation, a Blob object logically
                 * points to the BLOB value on the server rather than containing
                 * its binary data, greatly improving efficiency. The Blob
                 * interface provides methods for materializing the BLOB data on
                 * the client when that is desired.
                 */
                case Types.BLOB: {
                    Blob blob = result.getBlob(i);
                    if (blob != null) {
                        long n = blob.length();
                        if (n > Integer.MAX_VALUE) {
                            throw new IOException("can't process blob larger than Integer.MAX_VALUE");
                        }
                        values.add(blob.getBytes(1, (int) n));
                        blob.free();
                    }
                    break;
                }
                /**
                 * The JDBC type CLOB represents the SQL3 type CLOB (Character
                 * Large Object).
                 *
                 * A JDBC CLOB value is mapped to an instance of the Clob
                 * interface in the Java programming language. If a driver
                 * follows the standard implementation, a Clob object logically
                 * points to the CLOB value on the server rather than containing
                 * its character data, greatly improving efficiency. Two of the
                 * methods on the Clob interface materialize the data of a CLOB
                 * object on the client.
                 */
                case Types.CLOB: {
                    Clob clob = result.getClob(i);
                    if (clob != null) {
                        long n = clob.length();
                        if (n > Integer.MAX_VALUE) {
                            throw new IOException("can't process clob larger than Integer.MAX_VALUE");
                        }
                        values.add(clob.getSubString(1, (int) n));
                        clob.free();
                    }
                    break;
                }
                case Types.NCLOB: {
                    NClob nclob = result.getNClob(i);
                    if (nclob != null) {
                        long n = nclob.length();
                        if (n > Integer.MAX_VALUE) {
                            throw new IOException("can't process nclob larger than Integer.MAX_VALUE");
                        }
                        values.add(nclob.getSubString(1, (int) n));
                        nclob.free();
                    }
                    break;
                }
                /**
                 * The JDBC type DATALINK, new in the JDBC 3.0 API, is a column
                 * value that references a file that is outside of a data source
                 * but is managed by the data source. It maps to the Java type
                 * java.net.URL and provides a way to manage external files. For
                 * instance, if the data source is a DBMS, the concurrency
                 * controls it enforces on its own data can be applied to the
                 * external file as well.
                 *
                 * A DATALINK value is retrieved from a ResultSet object with
                 * the ResultSet methods getURL or object. If the Java
                 * platform does not support the type of URL returned by getURL
                 * or object, a DATALINK value can be retrieved as a String
                 * object with the method getString.
                 *
                 * java.net.URL values are stored in a database using the method
                 * setURL. If the Java platform does not support the type of URL
                 * being set, the method setString can be used instead.
                 *
                 *
                 */
                case Types.DATALINK: {
                    URL url = result.getURL(i);
                    values.add(url);
                    break;
                }
                /**
                 * The JDBC DATE type represents a date consisting of day,
                 * month, and year. The corresponding SQL DATE type is defined
                 * in SQL-92, but it is implemented by only a subset of the
                 * major databases. Some databases offer alternative SQL types
                 * that support similar semantics.
                 */
                case Types.DATE: {
                    try {
                        Date d = result.getDate(i);
                        values.add(d != null ? DateUtil.formatDateISO(d) : null);
                    } catch (SQLException e) {
                        values.add(null);
                    }
                    break;
                }
                case Types.TIME: {
                    try {
                        Time t = result.getTime(i);
                        values.add(t != null ? DateUtil.formatDateISO(t) : null);
                    } catch (SQLException e) {
                        values.add(null);
                    }
                    break;
                }
                case Types.TIMESTAMP: {
                    try {
                        Timestamp t = result.getTimestamp(i);
                        values.add(t != null ? DateUtil.formatDateISO(t) : null);
                    } catch (SQLException e) {
                        // java.sql.SQLException: Cannot convert value '0000-00-00 00:00:00' from column ... to TIMESTAMP.
                        values.add(null);
                    }
                    break;
                }
                /**
                 * The JDBC types DECIMAL and NUMERIC are very similar. They
                 * both represent fixed-precision decimal values.
                 *
                 * The corresponding SQL types DECIMAL and NUMERIC are defined
                 * in SQL-92 and are very widely implemented. These SQL types
                 * take precision and scale parameters. The precision is the
                 * total number of decimal digits supported, and the scale is
                 * the number of decimal digits after the decimal point. For
                 * most DBMSs, the scale is less than or equal to the precision.
                 * So for example, the value "12.345" has a precision of 5 and a
                 * scale of 3, and the value ".11" has a precision of 2 and a
                 * scale of 2. JDBC requires that all DECIMAL and NUMERIC types
                 * support both a precision and a scale of at least 15.
                 *
                 * The sole distinction between DECIMAL and NUMERIC is that the
                 * SQL-92 specification requires that NUMERIC types be
                 * represented with exactly the specified precision, whereas for
                 * DECIMAL types, it allows an implementation to add additional
                 * precision beyond that specified when the type was created.
                 * Thus a column created with type NUMERIC(12,4) will always be
                 * represented with exactly 12 digits, whereas a column created
                 * with type DECIMAL(12,4) might be represented by some larger
                 * number of digits.
                 *
                 * The recommended Java mapping for the DECIMAL and NUMERIC
                 * types is java.math.BigDecimal. The java.math.BigDecimal type
                 * provides math operations to allow BigDecimal types to be
                 * added, subtracted, multiplied, and divided with other
                 * BigDecimal types, with integer types, and with floating point
                 * types.
                 *
                 * The method recommended for retrieving DECIMAL and NUMERIC
                 * values is ResultSet.getBigDecimal. JDBC also allows access to
                 * these SQL types as simple Strings or arrays of char. Thus,
                 * Java programmers can use getString to receive a DECIMAL or
                 * NUMERIC result. However, this makes the common case where
                 * DECIMAL or NUMERIC are used for currency values rather
                 * awkward, since it means that application writers have to
                 * perform math on strings. It is also possible to retrieve
                 * these SQL types as any of the Java numeric types.
                 */
                case Types.DECIMAL:
                case Types.NUMERIC: {
                    BigDecimal bd = result.getBigDecimal(i);
                    values.add(bd == null ? null
                            : scale >= 0 ? bd.setScale(scale, rounding).doubleValue()
                            : bd.toString());
                    break;
                }
                /**
                 * The JDBC type DOUBLE represents a "double precision" floating
                 * point number that supports 15 digits of mantissa.
                 *
                 * The corresponding SQL type is DOUBLE PRECISION, which is
                 * defined in SQL-92 and is widely supported by the major
                 * databases. The SQL-92 standard leaves the precision of DOUBLE
                 * PRECISION up to the implementation, but in practice all the
                 * major databases supporting DOUBLE PRECISION support a
                 * mantissa precision of at least 15 digits.
                 *
                 * The recommended Java mapping for the DOUBLE type is as a Java
                 * double.
                 */
                case Types.DOUBLE: {
                    double d = result.getDouble(i);
                    values.add(d);
                    break;
                }
                /**
                 * The JDBC type FLOAT is basically equivalent to the JDBC type
                 * DOUBLE. We provided both FLOAT and DOUBLE in a possibly
                 * misguided attempt at consistency with previous database APIs.
                 * FLOAT represents a "double precision" floating point number
                 * that supports 15 digits of mantissa.
                 *
                 * The corresponding SQL type FLOAT is defined in SQL-92. The
                 * SQL-92 standard leaves the precision of FLOAT up to the
                 * implementation, but in practice all the major databases
                 * supporting FLOAT support a mantissa precision of at least 15
                 * digits.
                 *
                 * The recommended Java mapping for the FLOAT type is as a Java
                 * double. However, because of the potential confusion between
                 * the double precision SQL FLOAT and the single precision Java
                 * float, we recommend that JDBC programmers should normally use
                 * the JDBC DOUBLE type in preference to FLOAT.
                 */
                case Types.FLOAT: {
                    double d = result.getDouble(i);
                    values.add(d);
                    break;
                }
                /**
                 * The JDBC type INTEGER represents a 32-bit signed integer
                 * value ranging between -2147483648 and 2147483647.
                 *
                 * The corresponding SQL type, INTEGER, is defined in SQL-92 and
                 * is widely supported by all the major databases. The SQL-92
                 * standard leaves the precision of INTEGER up to the
                 * implementation, but in practice all the major databases
                 * support at least 32 bits.
                 *
                 * The recommended Java mapping for the INTEGER type is as a
                 * Java int.
                 */
                case Types.INTEGER: {
                    int n = result.getInt(i);
                    values.add(n);
                    break;
                }
                /**
                 * The JDBC type JAVA_OBJECT, added in the JDBC 2.0 core API,
                 * makes it easier to use objects in the Java programming
                 * language as values in a database. JAVA_OBJECT is simply a
                 * type code for an instance of a class defined in the Java
                 * programming language that is stored as a database object. The
                 * type JAVA_OBJECT is used by a database whose type system has
                 * been extended so that it can store Java objects directly. The
                 * JAVA_OBJECT value may be stored as a serialized Java object,
                 * or it may be stored in some vendor-specific format.
                 *
                 * The type JAVA_OBJECT is one of the possible values for the
                 * column DATA_TYPE in the ResultSet objects returned by various
                 * DatabaseMetaData methods, including getTypeInfo, getColumns,
                 * and getUDTs. The method getUDTs, part of the new JDBC 2.0
                 * core API, will return information about the Java objects
                 * contained in a particular schema when it is given the
                 * appropriate parameters. Having this information available
                 * facilitates using a Java class as a database type.
                 */
                case Types.OTHER:
                case Types.JAVA_OBJECT: {
                    Object o = result.getObject(i);
                    values.add(o);
                    break;
                }
                /**
                 * The JDBC type REAL represents a "single precision" floating
                 * point number that supports seven digits of mantissa.
                 *
                 * The corresponding SQL type REAL is defined in SQL-92 and is
                 * widely, though not universally, supported by the major
                 * databases. The SQL-92 standard leaves the precision of REAL
                 * up to the implementation, but in practice all the major
                 * databases supporting REAL support a mantissa precision of at
                 * least seven digits.
                 *
                 * The recommended Java mapping for the REAL type is as a Java
                 * float.
                 */
                case Types.REAL: {
                    float f = result.getFloat(i);
                    values.add(f);
                    break;
                }
                /**
                 * The JDBC type SMALLINT represents a 16-bit signed integer
                 * value between -32768 and 32767.
                 *
                 * The corresponding SQL type, SMALLINT, is defined in SQL-92
                 * and is supported by all the major databases. The SQL-92
                 * standard leaves the precision of SMALLINT up to the
                 * implementation, but in practice, all the major databases
                 * support at least 16 bits.
                 *
                 * The recommended Java mapping for the JDBC SMALLINT type is as
                 * a Java short.
                 */
                case Types.SMALLINT: {
                    int n = result.getInt(i);
                    values.add(n);
                    break;
                }
                case Types.SQLXML: {
                    SQLXML xml = result.getSQLXML(columns);
                    values.add(xml != null ? xml.getString() : null);
                    break;
                }
                /**
                 * The JDBC type TINYINT represents an 8-bit integer value
                 * between 0 and 255 that may be signed or unsigned.
                 *
                 * The corresponding SQL type, TINYINT, is currently supported
                 * by only a subset of the major databases. Portable code may
                 * therefore prefer to use the JDBC SMALLINT type, which is
                 * widely supported.
                 *
                 * The recommended Java mapping for the JDBC TINYINT type is as
                 * either a Java byte or a Java short. The 8-bit Java byte type
                 * represents a signed value from -128 to 127, so it may not
                 * always be appropriate for larger TINYINT values, whereas the
                 * 16-bit Java short will always be able to hold all TINYINT
                 * values.
                 */
                case Types.TINYINT: {
                    int n = result.getInt(i);
                    values.add(n);
                    break;
                }
                case Types.NULL: {
                    values.add(null);
                    break;
                }
                /**
                 * The JDBC type DISTINCT field (Types class)>DISTINCT
                 * represents the SQL3 type DISTINCT.
                 *
                 * The standard mapping for a DISTINCT type is to the Java type
                 * to which the base type of a DISTINCT object would be mapped.
                 * For example, a DISTINCT type based on a CHAR would be mapped
                 * to a String object, and a DISTINCT type based on an SQL
                 * INTEGER would be mapped to an int.
                 *
                 * The DISTINCT type may optionally have a custom mapping to a
                 * class in the Java programming language. A custom mapping
                 * consists of a class that implements the interface SQLData and
                 * an entry in a java.util.Map object.
                 */
                case Types.DISTINCT: {
                    values.add(null);
                    break;
                }
                /**
                 * The JDBC type STRUCT represents the SQL99 structured type. An
                 * SQL structured type, which is defined by a user with a CREATE
                 * TYPE statement, consists of one or more attributes. These
                 * attributes may be any SQL data type, built-in or
                 * user-defined.
                 *
                 * The standard mapping for the SQL type STRUCT is to a Struct
                 * object in the Java programming language. A Struct object
                 * contains a value for each attribute of the STRUCT value it
                 * represents.
                 *
                 * A STRUCT value may optionally be custom mapped to a class in
                 * the Java programming language, and each attribute in the
                 * STRUCT may be mapped to a field in the class. A custom
                 * mapping consists of a class that implements the interface
                 * SQLData and an entry in a java.util.Map object.
                 *
                 *
                 */
                case Types.STRUCT: {
                    values.add(null);
                    break;
                }
                case Types.REF: {
                    values.add(null);
                    break;
                }
                case Types.ROWID: {
                    values.add(null);
                    break;
                }
                default: {
                    values.add(null);
                    break;
                }
            }
        }
    }

    /**
     * Close result set
     *
     * @param result
     * @throws SQLException
     */
    public void close(ResultSet result) throws SQLException {
        result.close();
    }

    /**
     * Close statement
     *
     * @param statement
     * @throws SQLException
     */
    public void close(PreparedStatement statement) throws SQLException {
        statement.close();
    }

    /**
     * Close connection
     *
     * @param connection
     * @throws SQLException
     */
    public void close(Connection connection) throws SQLException {
        connection.close();
    }

    private void bind(PreparedStatement pstmt, int i, Object value) throws SQLException {
        if (value == null) {
            pstmt.setNull(i, Types.VARCHAR);
        } else if (value instanceof String) {
            String s = (String) value;
            if ("$now".equals(s)) {
                pstmt.setDate(i, new Date(new java.util.Date().getTime()));
            } else {
                pstmt.setString(i, (String) value);
            }
        } else if (value instanceof Integer) {
            pstmt.setInt(i, ((Integer) value).intValue());
        } else if (value instanceof Long) {
            pstmt.setLong(i, ((Long) value).longValue());
        } else if (value instanceof BigDecimal) {
            pstmt.setBigDecimal(i, (BigDecimal) value);
        } else if (value instanceof Date) {
            pstmt.setDate(i, (Date) value);
        } else if (value instanceof Timestamp) {
            pstmt.setTimestamp(i, (Timestamp) value);
        } else if (value instanceof Float) {
            pstmt.setFloat(i, ((Float) value).floatValue());
        } else if (value instanceof Double) {
            pstmt.setDouble(i, ((Double) value).doubleValue());
        } else {
            pstmt.setObject(i, value);
        }
    }

    public SQLService setRounding(String rounding) {
        if ("ceiling".equalsIgnoreCase(rounding)) {
            this.rounding = BigDecimal.ROUND_CEILING;
        } else if ("down".equalsIgnoreCase(rounding)) {
            this.rounding = BigDecimal.ROUND_DOWN;
        } else if ("floor".equalsIgnoreCase(rounding)) {
            this.rounding = BigDecimal.ROUND_FLOOR;
        } else if ("halfdown".equalsIgnoreCase(rounding)) {
            this.rounding = BigDecimal.ROUND_HALF_DOWN;
        } else if ("halfeven".equalsIgnoreCase(rounding)) {
            this.rounding = BigDecimal.ROUND_HALF_EVEN;
        } else if ("halfup".equalsIgnoreCase(rounding)) {
            this.rounding = BigDecimal.ROUND_HALF_UP;
        } else if ("unnecessary".equalsIgnoreCase(rounding)) {
            this.rounding = BigDecimal.ROUND_UNNECESSARY;
        } else if ("up".equalsIgnoreCase(rounding)) {
            this.rounding = BigDecimal.ROUND_UP;
        }
        return this;
    }

    public SQLService setPrecision(int scale) {
        this.scale = scale;
        return this;
    }
}