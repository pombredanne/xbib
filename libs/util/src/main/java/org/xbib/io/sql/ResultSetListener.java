package org.xbib.io.sql;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetListener {

    void received(ResultSet set) throws SQLException, IOException;

    void close(ResultSet set) throws SQLException, IOException;
}
