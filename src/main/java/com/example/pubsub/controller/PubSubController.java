package com.example.pubsub.controller;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/pubsub")
public class PubSubController {

    @Autowired
    private Publisher publisher;

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
}
