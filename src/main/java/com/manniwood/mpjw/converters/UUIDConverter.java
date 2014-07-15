package com.manniwood.mpjw.converters;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UUIDConverter extends BaseConverter<UUID>{

    @Override
    public void setItem(PreparedStatement pstmt, int i, UUID t) throws SQLException {
        pstmt.setObject(i, t);
    }

    @Override
    public UUID getItem(ResultSet rs, int i)  throws SQLException {
        return (UUID) rs.getObject(i);
    }
}
