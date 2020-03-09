## export

```
docker exec -i <CONTAINER_ID> mongoexport --db moneywatch --collection=banks > banks.json
docker exec -i <CONTAINER_ID> mongoexport --db moneywatch --collection=users > users.json
```

## import

```
docker exec -i <CONTAINER_ID> sh -c 'mongoimport --db moneywatch --collection=banks' < banks.json
docker exec -i <CONTAINER_ID> sh -c 'mongoimport --db moneywatch --collection=users' < users.json
```
