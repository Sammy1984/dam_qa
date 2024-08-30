package ru.spice.at.db.mappers;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import ru.spice.at.db.dto.MetadataDto;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MetadataMapper implements RowMapper<MetadataDto> {
    @Override
    public MetadataDto map(ResultSet rs, StatementContext ctx) throws SQLException {
        return MetadataDto.builder()
                .id(rs.getString("id"))
                .key(rs.getString("key"))
                .url(rs.getString("url"))
                .format(rs.getString("format"))
                .size(rs.getInt("size"))
                .width(rs.getInt("width"))
                .height(rs.getInt("height"))
                .resolution(rs.getString("resolution"))
                .description(rs.getString("description"))
                .keywords(rs.getString("keywords"))
                .createdAt(rs.getString("created_at"))
                .updatedAt(rs.getString("updated_at"))
                .filename(rs.getString("filename"))
                .received(rs.getString("received"))
                .sourceId(rs.getString("source_id"))
                .statusId(rs.getString("status_id"))
                .qualityId(rs.getString("quality_id"))
                .isMainImage(rs.getBoolean("is_main_image"))
                .isRawImage(rs.getBoolean("is_raw_image"))
                .isOwnTrademark(rs.getBoolean("is_own_trademark"))
                .isCopyright(rs.getBoolean("is_copyright"))
                .assigneeId(rs.getString("assignee_id"))
                .masterCategoryId(rs.getString("master_category_id"))
                .priority(rs.getInt("priority"))
                .createdBy(rs.getString("created_by"))
                .updatedBy(rs.getString("updated_by"))
                .originFilename(rs.getString("origin_filename"))
                .masterSellerId(rs.getInt("master_seller_id"))
                .externalTaskId(rs.getInt("external_task_id"))
                .externalDraftDone(rs.getBoolean("external_draft_done"))
                .externalOfferId(rs.getBigDecimal("external_offer_id"))
                .externalOfferName(rs.getString("external_offer_name"))
                .hash(rs.getString("hash"))
                .isDuplicate(rs.getBoolean("is_duplicate"))
                .importTypeId(rs.getString("import_type_id"))
                .build();
    }
}