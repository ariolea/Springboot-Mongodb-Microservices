package com.tvmaze;

import static org.assertj.core.api.Assertions.assertThat;

import com.tvmaze.client.TvMazeClient;
import com.tvmaze.config.TvMazeProperties;
import com.tvmaze.persistence.repository.ShowCacheRepository;
import com.tvmaze.web.ShowController;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

/**
 * El contexto levanta completo y la configuracion se enlaza como se espera.
 *
 * <p>La creacion automatica de indices se desactiva aqui porque exigiria una
 * conexion real a MongoDB durante el arranque del contexto de prueba.
 */
@SpringBootTest(properties = "spring.data.mongodb.auto-index-creation=false")
class TvMazeMiddlewareApplicationTests {

    @Autowired
    private ShowController showController;

    @Autowired
    private TvMazeClient tvMazeClient;

    @Autowired
    private ShowCacheRepository showCacheRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private TvMazeProperties properties;

    @Test
    @DisplayName("El contexto de la aplicacion carga con todos sus componentes")
    void contextLoads() {
        assertThat(showController).isNotNull();
        assertThat(tvMazeClient).isNotNull();
        assertThat(showCacheRepository).isNotNull();
        assertThat(cacheManager.getCacheNames()).containsExactly("showSearch");
    }

    @Test
    @DisplayName("Las propiedades de TVmaze se enlazan desde application.yml")
    void propertiesAreBound() {
        assertThat(properties.baseUrl()).isEqualTo("https://api.tvmaze.com");
        assertThat(properties.connectTimeout()).isEqualTo(Duration.ofSeconds(3));
        assertThat(properties.readTimeout()).isEqualTo(Duration.ofSeconds(10));
        assertThat(properties.cache().maximumSize()).isEqualTo(500L);
        assertThat(properties.cache().timeToLive()).isEqualTo(Duration.ofMinutes(10));
    }
}
