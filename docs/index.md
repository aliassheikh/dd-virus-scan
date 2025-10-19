dd-virus-scan
=============

Service for scanning Dataverse datasets for virus

Purpose
-------

Service for scanning Dataverse datasets for virus.

Interfaces
----------
This service has the following interfaces:

![interfaces](img/overview.png){width="70%"}

### Provided interfaces

#### Step invocation API

* _Protocol type_: HTTP
* _Internal or external_: **internal**
* _Purpose_: invoked by Dataverse when the `PrePublishDataset` workflow is executed

#### Admin console

* _Protocol type_: HTTP
* _Internal or external_: **internal**
* _Purpose_: application monitoring and management

### Consumed interfaces

#### Dataverse API

* _Protocol type_: HTTP
* _Internal or external_: **internal**
* _Purpose_: to retrieve file data to scan and to resume the workflow

Processing
----------

The service uses the `clamd` daemon for the actual scanning. The data files of the dataset will be streamed through the `clamd` deamon one by one. If no virus
is found the resume status will be _"Success"_, otherwise _"Failure"_ with in the message the instantiated `resultPostiveMessageTemplate`. See the comments in
the `config.yml` for details.

The service has the following thread pools:

* Workers for incoming HTTP requests from Dataverse. These will schedule scan tasks.
* Scan task workers. These will loop over all the files in the targeted dataset and stream them through `scand`, gathering the results. The overall result will
  be determined a resumption of the workflow will be scheduled as a resume task.
* Resume task workers. These will try to resume the workflow. This will be tried a configured number of times because Dataverse has known synchronization issues
  in its workflow framework.

References
----------

* <https://linux.die.net/man/8/clamd>{:target="_blank"}

