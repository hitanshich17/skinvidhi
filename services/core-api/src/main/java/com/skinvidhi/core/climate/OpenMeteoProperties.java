package com.skinvidhi.core.climate;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Open-Meteo endpoints and timeouts, bound from skinvidhi.open-meteo.* */
@ConfigurationProperties(prefix = "skinvidhi.open-meteo")
public record OpenMeteoProperties(String geocodingUrl, String forecastUrl, String airQualityUrl,
                                  Duration connectTimeout, Duration readTimeout, Duration cacheFor) {
}
