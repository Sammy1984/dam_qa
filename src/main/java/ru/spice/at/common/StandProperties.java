package ru.spice.at.common;

import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.aeonbits.owner.ConfigFactory;
import ru.spice.at.common.dto.*;
import ru.spice.at.common.emuns.Role;
import ru.spice.at.common.utils.auth.AuthUtils;
import ru.spice.at.common.utils.auth.KeycloakUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static ru.spice.at.common.utils.CipherHelper.decrypt;
import static ru.spice.at.common.utils.auth.KeycloakUtils.KEYCLOAK;

/**
 * Стендовые данные
 * @author Aleksandr Osokin
 */
@Log4j2
public class StandProperties {
    private static final String STAND_JSON_PATH = "src/test/resources/stand/%s.json";
    private final Settings settings;
    private StandData standData;

    public StandProperties() {
        settings = ConfigFactory.create(Settings.class, System.getenv(), System.getProperties());
        loadStandData();
    }

    private void loadStandData() {
        Path standJsonPath = Paths.get(".").toAbsolutePath()
                .normalize().resolve(String.format(STAND_JSON_PATH, settings.standProperties())).toAbsolutePath();
        try (Reader reader = new FileReader(standJsonPath.toString())) {
            Gson gson = new Gson();
            standData = gson.fromJson(reader, StandData.class);
        } catch (FileNotFoundException e) {
            log.error("Файл {} не найден", standJsonPath, e);
        } catch (IOException e) {
            log.error("Ошибка разбора json {}", standJsonPath, e);
        }
    }

    public Settings getSettings() {
        return settings;
    }

    public String getStand() {
        return standData.stand();
    }

    public String getUri() {
        return standData.baseUri();
    }

    public String getFrontendUri() {
        return standData.frontendUri();
    }

    public List<User> getUsers() {
        return standData.users();
    }

    public AuthUtils getAuthUtils(Role role) {
        User user = getUsers().stream().filter(u -> u.role().equals(role)).findFirst().
                orElseThrow(() -> new RuntimeException(String.format("Пользователь '%s' с ролью не найден", role.getName())));
        return getAuthUtils(user);
    }

    public AuthUtils getAuthUtils(User user) {
        if (standData.authCredentials().alias().equalsIgnoreCase(KEYCLOAK)) {
            return new KeycloakUtils(standData.authCredentials().authUrl(),
                    user.username(),
                    decrypt(user.password()));
        }
        return null;
    }

    public Remote getRemoteConnect() {
        return standData.remote();
    }

    public Kafka getKafkaConnect() {
        return standData.kafka();
    }

    public DbConfig getDbConfig() {
        return standData.dbConfig();
    }

    public KubernetesConfig kubernetesConfig() {return standData.kubernetesConfig();}
}