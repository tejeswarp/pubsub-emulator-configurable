package com.example.pubsub.config;

import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.ProjectTopicName;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PubSubConfig {

    private final PubSubProperties properties;

    public PubSubConfig(PubSubProperties properties) {
        this.properties = properties;
    }

    @Bean
    public Publisher publisher() throws Exception {
        ManagedChannel channel = ManagedChannelBuilder
                .forTarget(properties.getEmulatorHost())
                .usePlaintext()
                .build();

        FixedTransportChannelProvider channelProvider = FixedTransportChannelProvider.create(
                GrpcTransportChannel.create(channel)
        );

        ProjectTopicName topicName = ProjectTopicName.of(properties.getProjectId(), properties.getTopicId());

        return Publisher.newBuilder(topicName)
                .setChannelProvider(channelProvider)
                .setCredentialsProvider(NoCredentialsProvider.create())
                .build();
    }
}
