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

            ProjectTopicName topicName = ProjectTopicName.of(properties.getProjectId(), properties.getTopicId());
            ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(
                    properties.getProjectId(), properties.getSubscriptionId());

            try {
                topicAdminClient.createTopic(topicName);
            } catch (Exception e) {
                System.out.println("Topic exists or error: " + e.getMessage());
            }

            try {
                subscriptionAdminClient.createSubscription(
                        subscriptionName,
                        topicName,
                        PushConfig.getDefaultInstance(),
                        10
                );
            } catch (Exception e) {
                System.out.println("Subscription exists or error: " + e.getMessage());
            }
        }
    }
}
