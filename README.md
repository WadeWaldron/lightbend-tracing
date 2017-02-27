# Lightbend Production Monitoring
This small sample is used to demonstrate some of the features of the Lightbend
Production Monitoring offering.

It leverages Akka Distributed Pub Sub to create a cluster of applications.
By using the Distributed Pub Sub we are able to demonstrate how the various
features of the application work within a cluster.

This application is currently configured to run against the Lightbend Production
Suite sandbox. For more information see here:

https://developer.lightbend.com/docs/monitoring/latest/sandbox/start.html

It consists of 4 applications.

## Applications

### Producer
The producer can be run with the command `sbt produce`. This will cause it to
produce requests on a schedule. There are normal requests that come with one
frequency and then there are bursts that have a different frequency.

### Consumer
The consumer will consume messages that are created by the Requests. It can
be run with the command `sbt consume`. It consumes messages as fast as it can.
There are different message types and it will consume all of them.

### Blocking
This is a variation on the consumer. It can be run with `sbt blocking`. This
will consume messages but for certain message types it will block the thread
for a period of time before continuing. This can be used to demonstrate
the effect of a blocking operation on things such as mailbox sizes, dispatchers
etc.

### Failing
This is another variation on the consumer. It can be run with `sbt failing`.
This will consume messages but a certain percentage of those messages will fail.
This can be used to demonstrate how failures show up in the monitoring system,
both as metrics and as events.

## Lightbend Production Monitoring Features

### Distributed MDC logging
The application leverages MDC. It can be used to demonstrate how the MDC is
carried across from one application to the next so you can use it to trace the
path of a workflow through the system.

### Metrics
It is designed to connect with the Lightbend Monitoring Sandbox. This allows
it to expose various actor metrics that can be viewed in Grafana.

### Events
In addition to Metrics in Grafana it will also push events such as exceptions
into Kibana.

