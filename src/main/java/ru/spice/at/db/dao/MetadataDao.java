package ru.spice.at.db.dao;

import io.qameta.allure.Step;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import ru.spice.at.db.dto.MetadataDto;
import ru.spice.at.db.dto.StatusDto;
import ru.spice.at.db.mappers.MetadataMapper;
import ru.spice.at.db.mappers.StatusMapper;

import java.util.List;

public interface MetadataDao {
    @Step("Получение статусов")
    @SqlQuery("SELECT * FROM status")
    @RegisterRowMapper(StatusMapper.class)
    List<StatusDto> getStatuses();

    @Step("Получение метадаты для файлов с master_seller_id = {masterSellerId}")
    @SqlQuery("SELECT * FROM metadata WHERE master_seller_id = ?")
    @RegisterRowMapper(MetadataMapper.class)
    List<MetadataDto> getMetadataByMasterSellerId(Integer masterSellerId);
}