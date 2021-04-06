#!/bin/sh

CWD=`pwd`

cd ../graphbrain/docs
make html

cd $CWD
cp -r ../graphbrain/docs/build/html/* .

git add *
git commit -am "automatic deploy"
git push
