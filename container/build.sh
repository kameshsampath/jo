#!/bin/bash

set -eu

set -o pipefail

docker build -t quay.io/kameshsampath/jbang-action .

# docker push quay.io/kameshsampath/jbang-action