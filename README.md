# rss-to-mail

Read from RSS feeds and send an email for every new item.

# Migrating the database

```
ALTER TABLE CHANNEL ALTER COLUMN ID RESTART WITH (SELECT MAX(ID) + 1 FROM CHANNEL)

ALTER TABLE FEED_ITEM  ALTER COLUMN ID RESTART WITH (SELECT MAX(ID) + 1 FROM FEED_ITEM)
```

# Resources

* <https://blogtrottr.com/>
* <https://getrssfeed.com/>
* <https://zapier.com/>
