package com.skinvidhi.core.climate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.core.StringRedisTemplate;

/** Spring can build the climate beans from application.yml-style properties (no database or Docker needed). */
class ClimateWiringTest {

    @EnableConfigurationProperties(OpenMeteoProperties.class)
    static class Properties {
    }

    @Test
    void springCanCreateTheClimateBeans() {
        new ApplicationContextRunner()
                .withUserConfiguration(Properties.class, OpenMeteoClient.class, ClimateService.class)
                .withBean(StringRedisTemplate.class, () -> mock(StringRedisTemplate.class))
                .withBean(ObjectMapper.class, ObjectMapper::new)
                .withPropertyValues("skinvidhi.open-meteo.geocoding-url=https://geo.test",
                        "skinvidhi.open-meteo.forecast-url=https://wx.test", "skinvidhi.open-meteo.air-quality-url=https://air.test",
                        "skinvidhi.open-meteo.connect-timeout=2s", "skinvidhi.open-meteo.read-timeout=5s",
                        "skinvidhi.open-meteo.cache-for=24h")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(ClimateService.class);
                });
    }
}
