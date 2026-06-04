package com.finance.transactional.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ar.aging")
public class ArAgingProperties {

    /** Upper day boundaries for buckets, e.g. 30,60,90,120 → Current, 1-30, 31-60, 61-90, 91-120, Over 120 */
    private String defaultBucketDays = "30,60,90,120";

    public List<Integer> parseBucketDays(String override) {
        String raw = override != null && !override.isBlank() ? override : defaultBucketDays;
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .sorted()
                .collect(Collectors.toList());
    }
}
