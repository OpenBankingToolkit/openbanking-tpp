#!/bin/bash
echo "////////////////IMPORT BANKS DATA/////////////////"
mongoimport --db moneywatch --collection=banks < /docker-entrypoint-initdb.d/banks.json
echo "////////////////IMPORT USERS DATA/////////////////"
mongoimport --db moneywatch --collection=users < /docker-entrypoint-initdb.d/users.json
echo "////////////////IMPORT FINISHED/////////////////"