package ru.spice.at.db.utils;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import ru.spice.at.common.StandProperties;
import ru.spice.at.common.dto.DbConfig;
import ru.spice.at.common.utils.kubernetes.KubernetesHelper;

import javax.sql.DataSource;
import java.util.*;

/**
 * Утилитный класс для подключения к БД
 */
@Log4j2
public class DbHelper {
    private static Jdbi connection;

    /**
     * Создаем подключение к БД
     *
     * @param daoClass dao класс с запросами к БД
     * @return dao объект
     */
    public static <T> T createDbo(Class<T> daoClass) {
        return getConnection().onDemand(daoClass);
    }

    private static Jdbi getConnection() {
        DbConfig config = new StandProperties().getDbConfig();
        if (config.host().equalsIgnoreCase("localhost")) {
            KubernetesHelper.portForward(Integer.parseInt(config.port()));
        }
        if (connection == null) {
            log.info("Подключаемся к бд {}:{}/{}", config.host(), config.port(), config.dbName());
            Jdbi connect;
            switch (config.type().toLowerCase()) {
                case "postgres":
                    String url = String.format("jdbc:postgresql://%s:%s/%s",
                            config.host(), config.port(), config.dbName());
                    connect = Jdbi.create(url, createDataSourcePostgres(config)).installPlugin(new PostgresPlugin());
                    break;
                case "oracle":
                    connect = Jdbi.create(createDataSourceOracle(config));
                    break;
                default:
                    log.error(config.type().toLowerCase() + " база данных не поддерживается");
                    throw new RuntimeException(config.type().toLowerCase() + " база данных не поддерживается");
            }
            connect.installPlugin(new SqlObjectPlugin());
            connection = connect;
        }
        return connection;
    }

    @SneakyThrows
    private static DataSource createDataSourceOracle(DbConfig config) {
        PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();
        pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        String url = String.format("jdbc:oracle:thin:@%s:%s:%s", config.host(), config.port(), config.dbName());
        pds.setURL(url);
        pds.setUser(config.username());
        pds.setPassword(config.password());
        pds.setConnectionPoolName("JDBC_UCP_POOL");
        pds.setInactiveConnectionTimeout(10);
        return pds;
    }

    @SneakyThrows
    private static Properties createDataSourcePostgres(DbConfig config) {
        Properties props = new Properties();
        props.setProperty("user", config.username());
        props.setProperty("password", config.password());
        props.setProperty("stringtype", "unspecified");
        return props;
    }
}