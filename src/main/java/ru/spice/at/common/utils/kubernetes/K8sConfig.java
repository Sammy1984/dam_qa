package ru.spice.at.common.utils.kubernetes;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.KubeConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;

import static ru.spice.at.api.utils.ApiUtils.getPath;

/**
 * Класс для конфигурации Api клиента для kubernetes
 */
@Slf4j
final class K8sConfig {
    private static final String K8S_CONFIG_PATH = "txt/config/k8sConfig.txt";

    @Getter
    private final ApiClient apiClient;
    @Getter
    private final CoreV1Api coreV1Api;

    public K8sConfig() {
        try {
            String config = new String(Files.readAllBytes(getPath(K8S_CONFIG_PATH)));
            this.apiClient = Config.fromConfig(KubeConfig.loadKubeConfig(new StringReader(config)));
            this.apiClient.setConnectTimeout(60000);
            this.apiClient.setReadTimeout(60000);
            this.apiClient.setWriteTimeout(60000);
            Configuration.setDefaultApiClient(apiClient);
        } catch (IOException e) {
            log.error("K8sConfig: Api клиент не инициализирован");
            throw new RuntimeException("K8sConfig: Api клиент не инициализирован", e);
        }
        this.coreV1Api = new CoreV1Api(apiClient);
    }
}
