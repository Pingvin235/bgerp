#!/bin/bash

# cleanup all references to non-existent remote branches
git remote prune origin
# cleanup local branches that no longer exist on remote
git branch -vv | grep -P '^\s+p' | grep 'gone]' | awk '{print $1}' | xargs git branch -D
# cleanup the repo from unreferenced objects
git reflog expire --expire-unreachable=now --all && git gc --prune=now
# cleanup unused GIT LFS files
git lfs prune
