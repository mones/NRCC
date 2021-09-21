#!/bin/sh

usage() {
  echo "Usage: $0 -s server [-i iterations]"
  echo "Note: default is 1 million iterations"
  exit 1
}

SERVER=""
ITERATIONS="1000000"
JAR="nrcc-1.0-client.jar"

test ! -f ${JAR} && echo "Missing client jar: ${JAR}" && exit 1

while getopts ":s:i:" opt; do
  case "${opt}" in
    s) SERVER=${OPTARG}
    ;;
    i) ITERATIONS=${OPTARG}
    ;;
    *) usage
    ;;
  esac
done

test -z "${SERVER}" && echo "Missing server (forgot -s option?)" && exit 1

java -jar ${JAR} -s ${SERVER} -c ${ITERATIONS} -r 0:1000000 &
java -jar ${JAR} -s ${SERVER} -c ${ITERATIONS} -r 0:2000000 &
java -jar ${JAR} -s ${SERVER} -c ${ITERATIONS} -r 0:3000000 &
java -jar ${JAR} -s ${SERVER} -c ${ITERATIONS} -r 0:4000000 &
java -jar ${JAR} -s ${SERVER} -c ${ITERATIONS} -r 0:5000000 &
