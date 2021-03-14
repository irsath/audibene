This solution:
* doesn't handle update on existing users on the api neither user deletion
* duplicate event might be published in pulsar for users in the last page of the api
* is not resilient in case of any failure
* doesn't persist it state (so a reboot will push back every user into pulsar)
* may have a high memory footprint on first run
* suppose that the number of item returned by a get on users resource is constant and defined by the "limit" property within the http response

Performance can be improved by:
* using async http call to gorest api
* using multiple thread to fetch from gorest api / produce to pulsar
* fine-tuning the scheduling
* using streaming http calls
* using a compact serialization format like Avro with pulsar

Memory footprint can be improved by:
* streaming api http call to pulasr
* persisting the AlreadyKnownUsers structure

Code quality can be improved by:
* tests (of course)
* using a structured serialization format like Avro from which we can generate java classes
* writing a proper pulsar connector (https://pulsar.apache.org/docs/en/io-overview/) 
