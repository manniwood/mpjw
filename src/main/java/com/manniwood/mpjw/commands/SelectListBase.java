package com.manniwood.mpjw.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.manniwood.mpjw.SQLTransformer;
import com.manniwood.mpjw.TransformedSQL;
import com.manniwood.mpjw.converters.ConverterStore;

public abstract class SelectListBase<T> implements Command {

    protected final ConverterStore converterStore;
    protected final String sql;
    protected final Connection conn;
    protected final Class<T> returnType;

    protected PreparedStatement pstmt;

    /**
     * Return object.
     */
    protected List<T> list;

    public SelectListBase(
            ConverterStore converterStore,
            String sql,
            Connection conn,
            Class<T> returnType) {
        super();
        this.converterStore = converterStore;
        this.sql = sql;
        this.conn = conn;
        this.returnType = returnType;
    }

    @Override
    public String getSQL() {
        return sql;
    }

    @Override
    public Connection getConnection() {
        return conn;
    }

    @Override
    public PreparedStatement getPreparedStatement() {
        return pstmt;
    }

    @Override
    public void execute() throws SQLException {
        TransformedSQL tsql = SQLTransformer.transform(sql);
        pstmt = conn.prepareStatement(tsql.getSql());
        setSQLArguments(tsql);
        populateList();
    }

    protected abstract void setSQLArguments(TransformedSQL tsql) throws SQLException;
    protected abstract void populateList() throws SQLException;

    public List<T> getResult() {
        return list;
    }
}
