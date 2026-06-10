package com.wang.search.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * Elasticsearch 配置
 * 开发环境使用 HTTP 连接 + 禁用 SSL 验证（ES 8.x 默认启用 HTTPS）
 * 生产环境应配置正确的 SSL 证书，并使用 HTTPS
 */
@Configuration
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String uris;

    @Value("${spring.elasticsearch.username:elastic}")
    private String username;

    @Value("${spring.elasticsearch.password:changeme}")
    private String password;

    @Override
    public ClientConfiguration clientConfiguration() {
        String hostAndPort = uris.replace("http://", "").replace("https://", "");

        ClientConfiguration.MaybeSecureClientConfigurationBuilder builder = ClientConfiguration.builder()
                .connectedTo(hostAndPort);

        // 如果是 HTTPS 连接，禁用 SSL 证书验证（开发环境）
        if (uris.startsWith("https://")) {
            builder.usingSsl(createSSLContext());
        }

        builder.withBasicAuth(username, password);

        return builder.build();
    }

    /**
     * 创建信任所有证书的 SSLContext（仅用于开发环境）
     */
    private SSLContext createSSLContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, null);
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to create SSLContext", e);
        }
    }
}
