Setting up Pub/Sub in local
docker run -d -p 8681:8681 --name pubsub-emulator google/cloud-sdk:latest gcloud beta emulators pubsub start --host-port=0.0.0.0:8681
Testing the Pub-Sub
Method: POST
URL: http://localhost:8080/api/pubsub/publish-message?message=HelloSubscriber1