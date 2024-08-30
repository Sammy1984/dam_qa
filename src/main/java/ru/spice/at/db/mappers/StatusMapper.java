package ru.spice.at.db.mappers;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import ru.spice.at.db.dto.StatusDto;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StatusMapper implements RowMapper<StatusDto> {

    @Override
    public StatusDto map(ResultSet rs, StatementContext ctx) throws SQLException {
        return StatusDto.builder()
                .id(rs.getString("id"))
                .name(rs.getString("name"))
                .localName(rs.getString("local_name"))
                .createdAt(rs.getString("created_at"))
                .updatedAt(rs.getString("updated_at"))
                .build();
    }
}