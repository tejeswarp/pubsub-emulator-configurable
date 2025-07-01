package com.example.pubsub.config;

import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PushConfig;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class PubSubInitializer {

    private final PubSubProperties properties;

    public PubSubInitializer(PubSubProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void initialize() throws Exception {
        ManagedChannel channel = ManagedChannelBuilder
                .forTarget(properties.getEmulatorHost())
                .usePlaintext()
                .build();

        FixedTransportChannelProvider channelProvider = FixedTransportChannelProvider.create(
                GrpcTransportChannel.create(channel)
        );

        try (TopicAdminClient topicAdminClient = TopicAdminClient.create(
                TopicAdminSettings.newBuilder()
                        .setTransportChannelProvider(channelProvider)
                        .setCredentialsProvider(NoCredentialsProvider.create())
                        .build());
             SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create(
                     SubscriptionAdminSettings.newBuilder()
                             .setTransportChannelProvider(channelProvider)
                             .setCredentialsProvider(NoCredentialsProvider.create())
                             .build())) {

            // Create all required topics
            createTopicIfNotExists(topicAdminClient, properties.getTopicId());
            createTopicIfNotExists(topicAdminClient, properties.getUploadDocStatusTopic());
            createTopicIfNotExists(topicAdminClient, properties.getUploadDocRetryTopic());

            // Create subscription to main & status topic
            createSubscriptionIfNotExists(subscriptionAdminClient, properties.getSubscriptionId(), properties.getTopicId());
            createSubscriptionIfNotExists(subscriptionAdminClient, properties.getUploadDocStatusSubscription(), properties.getUploadDocStatusTopic());
        } finally {
            channel.shutdownNow(); // Clean up to avoid leak warning
        }
    }

    private void createTopicIfNotExists(TopicAdminClient client, String topicId) {
        ProjectTopicName topicName = ProjectTopicName.of(properties.getProjectId(), topicId);
        try {
            client.getTopic(topicName);
            System.out.println("Topic already exists: " + topicId);
        } catch (Exception e) {
            try {
                client.createTopic(topicName);
                System.out.println("Created topic: " + topicId);
            } catch (Exception ex) {
                System.out.println("Could not create topic: " + topicId + " - " + ex.getMessage());
            }
        }
    }

    private void createSubscriptionIfNotExists(SubscriptionAdminClient client, String subId, String topicId) {
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(properties.getProjectId(), subId);
        ProjectTopicName topicName = ProjectTopicName.of(properties.getProjectId(), topicId);
        try {
            client.getSubscription(subscriptionName);
            System.out.println("Subscription already exists: " + subId);
        } catch (Exception e) {
            try {
                client.createSubscription(
                        subscriptionName,
                        topicName,
                        PushConfig.getDefaultInstance(),
                        10
                );
                System.out.println("Created subscription: " + subId);
            } catch (Exception ex) {
                System.out.println("Could not create subscription: " + subId + " - " + ex.getMessage());
            }
        }
    }
}
