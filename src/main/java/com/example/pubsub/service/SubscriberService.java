package com.example.pubsub.service;

import com.example.pubsub.config.PubSubProperties;
import com.example.pubsub.model.UploadDocRequest;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service
public class SubscriberService {

    private final PubSubProperties properties;
    private final FileNetService fileNetService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SubscriberService(PubSubProperties properties, FileNetService fileNetService) {
        this.properties = properties;
        this.fileNetService = fileNetService;
    }

    @PostConstruct
    public void startSubscriber() {
        try {
            ManagedChannel channel = ManagedChannelBuilder
                    .forTarget(properties.getEmulatorHost())
                    .usePlaintext()
                    .build();

            ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(
                    properties.getProjectId(), properties.getSubscriptionId());

            MessageReceiver receiver = (PubsubMessage message, AckReplyConsumer consumer) -> {
                try {
                    String json = message.getData().toStringUtf8();
                    UploadDocRequest request = objectMapper.readValue(json, UploadDocRequest.class);
                    String partyId = request.getPartyId();
                    String fileName = request.getFileName();

                    String fileNetId = fileNetService.uploadDocument(partyId, fileName);
                    if (fileNetId != null) {
                        publishStatus(partyId, fileNetId);
                    } else {
                        publishRetry(json);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    publishRetry(message.getData().toStringUtf8());
                }
                consumer.ack();
            };

            Subscriber subscriber = Subscriber.newBuilder(subscriptionName, receiver)
                    .setChannelProvider(
                            FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel)))
                    .setCredentialsProvider(NoCredentialsProvider.create())
                    .build();

            subscriber.startAsync().awaitRunning();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void publishStatus(String partyId, String fileNetId) throws Exception {
        ProjectTopicName topicName = ProjectTopicName.of(
                properties.getProjectId(), properties.getUploadDocStatusTopic());

        // Create publisher and publish the message
        Publisher publisher = null;
        ManagedChannel channel = null;
        /*Below code has to be modified slightly when connecting to real GCP PUB/SUB*/
        try {
            channel = ManagedChannelBuilder
                    .forTarget(properties.getEmulatorHost())  // usually localhost:8085
                    .usePlaintext()
                    .build();

            FixedTransportChannelProvider channelProvider = FixedTransportChannelProvider.create(
                    GrpcTransportChannel.create(channel)
            );

            publisher = Publisher.newBuilder(topicName)
                    .setChannelProvider(channelProvider)              // required for emulator
                    .setCredentialsProvider(NoCredentialsProvider.create()) // avoid GCP auth
                    .setEnableMessageOrdering(true)
                    .build();

            String json = "{\"partyId\": \"" + partyId + "\", \"fileNetId\": \"" + fileNetId + "\"}";
            publisher.publish(PubsubMessage.newBuilder()
                    .setMessageId(UUID.randomUUID().toString())
                    .setOrderingKey(partyId)
                    .setData(ByteString.copyFromUtf8(json))
                    .build()).get();
            System.out.println("FileNetId published to status topic successfully: ");
        } finally {
            if (publisher != null) {
                publisher.shutdown(); // graceful shutdown
            }
            if (channel != null) {
                channel.shutdown(); // graceful shutdown
            }

        }
    }

    private void publishRetry(String jsonPayload) {
        try {
            UploadDocRequest request = objectMapper.readValue(jsonPayload, UploadDocRequest.class);
            ProjectTopicName topicName = ProjectTopicName.of(
                    properties.getProjectId(), properties.getUploadDocRetryTopic());

            // Create publisher and publish the message
            Publisher publisher = null;
            ManagedChannel channel = null;
            /*Below code has to be modified slightly when connecting to real GCP PUB/SUB*/
            try {
                channel = ManagedChannelBuilder
                        .forTarget(properties.getEmulatorHost())  // usually localhost:8085
                        .usePlaintext()
                        .build();

                FixedTransportChannelProvider channelProvider = FixedTransportChannelProvider.create(
                        GrpcTransportChannel.create(channel)
                );

                publisher = Publisher.newBuilder(topicName)
                        .setChannelProvider(channelProvider)              // required for emulator
                        .setCredentialsProvider(NoCredentialsProvider.create()) // avoid GCP auth
                        .setEnableMessageOrdering(true)
                        .build();

                publisher.publish(PubsubMessage.newBuilder()
                        .setMessageId(UUID.randomUUID().toString())
                                .setOrderingKey(request.getPartyId())
                        .setData(ByteString.copyFromUtf8(jsonPayload))
                        .build()).get();
                System.out.println("File published to retry topic successfully: ");
            } finally {
                if (publisher != null) {
                    publisher.shutdown(); // graceful shutdown
                }
                if (channel != null) {
                    channel.shutdown(); // graceful shutdown
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
