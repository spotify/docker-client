#!/bin/sh

secs=10
endTime=$(( $(date +%s) + secs ))

while [ $(date +%s) -lt $endTime ]; do
    echo "Orange and a lot of other colours and shapes and sounds"
    echo "Even more log output on another row"
    date
done

sleep 90

