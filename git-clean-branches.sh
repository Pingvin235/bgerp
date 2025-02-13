#!/bin/bash

git branch -vv | grep -P '^\s+p' | grep 'gone]' | awk '{print $1}' | xargs git branch -D