#!/bin/bash

usage() {
  echo "Usage: $0 -s server"
  exit 1
}

SERVER=""

while getopts ":s:" opt; do
  case "${opt}" in
    s) SERVER=${OPTARG}
    ;;
    *) usage
    ;;
  esac
done

test -z "${SERVER}" && echo "Missing server (forgot -s option?)" && exit 1

S="/inet/tcp/0/${SERVER}/4000"
while true; do
  N=$(printf "%09d\r\n" $RANDOM)
  awk -v Token=$N -v Service=$S "BEGIN{ print Token |& Service }"
done

