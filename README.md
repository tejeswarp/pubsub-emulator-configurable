# Project Title

Simple overview of use/purpose.

## Description

An in-depth paragraph about your project and overview of use.

## Getting Started

### Dependencies

### Installing

### Setting up Pub/Sub in local

docker run -d -p 8681:8681 --name pubsub-emulator google/cloud-sdk:latest gcloud beta emulators pubsub start --host-port=0.0.0.0:8681


### Testing the Pub-Sub

Method: POST

URL: http://localhost:8080/api/pubsub/publish-message?message=HelloSubscriber1

### Testing the Pub-Sub with JSON

Method: POST

URL: http://localhost:8080/api/pubsub/upload-doc

BODY:
{
"partyId": "P123",
"fileName": "document.pdf"
}


