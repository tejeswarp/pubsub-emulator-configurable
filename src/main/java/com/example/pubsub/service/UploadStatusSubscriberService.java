package com.example.pubsub.service;

import com.example.pubsub.config.PubSubProperties;
import com.example.pubsub.model.UploadStatusRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import com.google.cloud.pubsub.v1.AckReplyConsumer;

@Service
public class UploadStatusSubscriberService {

    private final PubSubProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UploadStatusSubscriberService(PubSubProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void startUploadStatusSubscriber() {
        try {
            ManagedChannel channel = ManagedChannelBuilder
                    .forTarget(properties.getEmulatorHost())
                    .usePlaintext()
                    .build();

            ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(
                    properties.getProjectId(), properties.getUploadDocStatusSubscription());

            MessageReceiver receiver = (PubsubMessage message, AckReplyConsumer consumer) -> {
                try {
                    String json = message.getData().toStringUtf8();
                    System.out.println("Received status message: " + json);

                    UploadStatusRequest status = objectMapper.readValue(json, UploadStatusRequest.class);

                    // Simulate internal processing
                    System.out.println("Processed: partyId = " + status.getPartyId() + ", fileNetId = " + status.getFileNetId());

                    consumer.ack();
                } catch (Exception e) {
                    e.printStackTrace();
                    consumer.nack();
                }
            };

            Subscriber subscriber = Subscriber.newBuilder(subscriptionName, receiver)
                    .setChannelProvider(
                            FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel)))
                    .setCredentialsProvider(NoCredentialsProvider.create())
                    .build();

            subscriber.startAsync().awaitRunning();
            System.out.println("📡 Listening to upload-doc-status-topic...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
