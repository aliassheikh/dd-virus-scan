dd-virus-scan
=============

![Build Status](https://github.com/DANS-KNAW/dd-virus-scan/actions/workflows/build.yml/badge.svg)
[![codecov](https://codecov.io/gh/DANS-KNAW/dd-virus-scan/branch/master/graph/badge.svg)](https://codecov.io/gh/DANS-KNAW/dd-virus-scan)
![Site Status](https://github.com/DANS-KNAW/dd-virus-scan/actions/workflows/docs.yml/badge.svg)

For documentation see: https://dans-knaw.github.io/dd-virus-scan

To run locally, start up clam av like this:

docker run -p 3310:3310 -v $(pwd)/clamd.conf:/etc/clamav/clamd.conf mkodockx/docker-clamav:alpine
.
