package com.example.pubsub.controller;


import com.example.pubsub.config.PubSubProperties;
import com.example.pubsub.model.UploadDocRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/pubsub")
public class PubSubController {

    private PubSubProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private Publisher publisher;

    public PubSubController(PubSubProperties properties) {
        this.properties = properties;
    }

    @PostMapping("/publish")
    public String publish() throws ExecutionException, InterruptedException {
        String message = "Hello From Docker";
        PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                .setData(ByteString.copyFromUtf8(message))
                .build();

        publisher.publish(pubsubMessage).get(); // Wait for publish to complete
        return "Published: " + message;
    }

    @PostMapping("/publish-message")
    @ResponseBody
    public String publish(@RequestParam("message") String message) throws ExecutionException, InterruptedException {
        PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                .setData(ByteString.copyFromUtf8(message))
                .build();
        publisher.publish(pubsubMessage).get(); // Wait for publish to complete
        return "Published: " + message;
    }

    @PostMapping("/upload-doc")
    public ResponseEntity<String> publishMessage(@RequestBody UploadDocRequest request) {
        try {
            // Convert UploadDocRequest object to JSON string
            String json = objectMapper.writeValueAsString(request);

            // Create Topic name reference
            ProjectTopicName topicName = ProjectTopicName.of(
                    properties.getProjectId(), properties.getTopicId());

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
                        .build();

                PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                        .setData(ByteString.copyFromUtf8(json))
                        .build();

                publisher.publish(pubsubMessage).get();  // Waits for the future to complete
            } finally {
                if (publisher != null) {
                    publisher.shutdown(); // graceful shutdown
                }
                if (channel != null) {
                    channel.shutdown(); // graceful shutdown
                }
            }

            return ResponseEntity.ok("Message published to upload-doc-topic");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to publish message");
        }
    }
}
