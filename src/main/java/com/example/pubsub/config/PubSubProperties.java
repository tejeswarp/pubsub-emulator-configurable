package com.example.pubsub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pubsub")
public class PubSubProperties {

    private String emulatorHost;
    private String projectId;
    private String topicId; // upload-doc-topic
    private String subscriptionId;
    private String uploadDocStatusTopic;
    private String uploadDocRetryTopic;
    private int maxRetries;

    public String getEmulatorHost() { return emulatorHost; }
    public void setEmulatorHost(String emulatorHost) { this.emulatorHost = emulatorHost; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getTopicId() { return topicId; }
    public void setTopicId(String topicId) { this.topicId = topicId; }

    public String getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; }

    public String getUploadDocStatusTopic() { return uploadDocStatusTopic; }
    public void setUploadDocStatusTopic(String uploadDocStatusTopic) { this.uploadDocStatusTopic = uploadDocStatusTopic; }

    public String getUploadDocRetryTopic() { return uploadDocRetryTopic; }
    public void setUploadDocRetryTopic(String uploadDocRetryTopic) { this.uploadDocRetryTopic = uploadDocRetryTopic; }

    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
}
