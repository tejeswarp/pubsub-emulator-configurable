package com.example.pubsub.service;

import com.example.pubsub.config.PubSubProperties;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
public class SubscriberService {

    private final PubSubProperties properties;

    public SubscriberService(PubSubProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void startSubscriber() {
        try {
            System.out.println("Starting Subscriber...");

            ManagedChannel channel = ManagedChannelBuilder
                    .forTarget(properties.getEmulatorHost())
                    .usePlaintext()
                    .build();

            FixedTransportChannelProvider channelProvider = FixedTransportChannelProvider.create(
                    GrpcTransportChannel.create(channel)
            );

            ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(
                    properties.getProjectId(), properties.getSubscriptionId());

            MessageReceiver receiver = (PubsubMessage message, com.google.cloud.pubsub.v1.AckReplyConsumer consumer) -> {
                System.out.println("Received message: " + message.getData().toStringUtf8());
                consumer.ack();
            };

            Subscriber subscriber = Subscriber.newBuilder(subscriptionName, receiver)
                    .setChannelProvider(channelProvider)
                    .setCredentialsProvider(NoCredentialsProvider.create())
                    .build();

            subscriber.startAsync().awaitRunning();
            System.out.println("Subscriber running on: " + subscriptionName);

        } catch (Exception e) {
            System.err.println("Subscriber error: " + e.getMessage());
        }
    }
}
