@echo off

if not exist %1 (
    echo "Please make sure the file exists and is compiled"
    exit 2
)
if not exist %2 (
    echo "Please provide a number of bodies as second argument"
    exit 1
)

set str1=Exectuing file
set str_new=%str1% %1

java %1 %2 56500 2 >> .\csv\p240.csv

echo %str_new%