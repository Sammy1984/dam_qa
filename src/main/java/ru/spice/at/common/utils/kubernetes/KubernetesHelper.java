package ru.spice.at.common.utils.kubernetes;

import com.google.common.io.ByteStreams;
import io.kubernetes.client.PortForward;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodStatus;
import lombok.extern.log4j.Log4j2;
import ru.spice.at.common.StandProperties;
import ru.spice.at.common.dto.KubernetesConfig;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

/**
 * Утилитный класс для работы с kubernetes (проброс портов)
 */
@Log4j2
public final class KubernetesHelper {
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public static void portForward(int port) {
        portForward(port, port);
    }

    public static void portForward(int internalPort, int containerPort) {
        KubernetesConfig kubernetesConfig = new StandProperties().kubernetesConfig();
        portForward(kubernetesConfig.namespace(), kubernetesConfig.label(), internalPort, containerPort);
    }

    /**
     * Выполняем проброску соединения на локальный порт
     *
     * @param namespace     - пространство подов
     * @param label         - лейбл с подом (Например, statefulset.kubernetes.io/pod-name=pod-name-0)
     * @param internalPort  - внутренний (локальный) свободный порт для переброски
     * @param containerPort - порт контейнера
     */
    public static void portForward(String namespace, String label, int internalPort, int containerPort) {
        log.info("Выполняем проброс порта: {}, label: {}, internalPort: {}, containerPort: {}", namespace, label, internalPort, containerPort);
        try {
            List<V1Pod> podList = getPodList(namespace, label);
            V1Pod pod = podList.stream().findFirst().orElseThrow(() -> new IOException("Первый под не найден"));
            log.info("Выбран под: {}", Objects.requireNonNull(pod.getSpec()).getNodeName());
            getK8sPortForward(pod, internalPort, containerPort);
        } catch (IOException | ApiException e) {
            String message = String.format("Ошибка проброса порта %s:%s до namespace %s. Ошибка: %s", containerPort, internalPort, namespace, e.getMessage());
            log.error(message);
            throw new RuntimeException(message, e);
        }
    }

    private static List<V1Pod> getPodList(final String namespace, final String label) {
        try {
            final V1PodList v1PodList = new K8sConfig().getCoreV1Api().listNamespacedPod(namespace, null, null, null,
                    null, label, null, null, null, null, null);
            return v1PodList.getItems().stream()
                    .filter(item -> {
                        final V1PodStatus status = item.getStatus();
                        if (nonNull(status)) {
                            final String phase = status.getPhase();
                            return nonNull(phase) && phase.equals("Running");
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        } catch (ApiException e) {
            throw new RuntimeException("Не получилось вызвать api k8s: " + e.getMessage());
        }
    }

    private static PortForward.PortForwardResult getK8sPortForward(final V1Pod pod, final int internalPort, final int containerPort) throws IOException, ApiException {
        final PortForward.PortForwardResult result = new PortForward().forward(pod, Arrays.asList(internalPort, containerPort));
        final ServerSocket ss = new ServerSocket(internalPort);
        final AtomicReference<Socket> s = new AtomicReference<>();
        final AtomicBoolean isOpen = new AtomicBoolean(true);

        executorService.execute(() -> {
            try {
                while (isOpen.get()) {
                    s.set(ss.accept());
                    ByteStreams.copy(s.get().getInputStream(), result.getOutboundStream(containerPort));
                }
            } catch (IOException e) {
                if (isOpen.get()) {
                    log.error("Froward error", e);
                }
            }
        });
        executorService.execute(() -> {
            try {
                while (isOpen.get()) {
                    if (nonNull(s.get())) {
                        ByteStreams.copy(result.getInputStream(containerPort), s.get().getOutputStream());
                    }
                }
            } catch (IOException e) {
                if (isOpen.get()) {
                    log.error("Forward error", e);
                }
            }
        });
        log.debug("Connect address: <Current Host> <{}>", internalPort);

        return result;
    }
}