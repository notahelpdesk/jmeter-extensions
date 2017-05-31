# JMeter Extensions

This repository is a collection of tools useful for working with [Apache JMeter](http://jmeter.apache.org/)

## HMAC Helper

This extension was created to work with the [Adyen Payment Provider](https://docs.adyen.com/developers/payments).

By using this you can simulate a response from the payment provider without actually having to make requests to the provider.

### Dependencies

This helper depends on the [Apache Commons Codec](https://commons.apache.org/proper/commons-codec/) for Base64 encoding.