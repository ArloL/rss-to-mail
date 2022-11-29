-- !! This file uses two semicolons as separators !!

DROP TABLE IF EXISTS "channel";;

DROP TABLE IF EXISTS "feed_item";;

CREATE TABLE "channel" (
	"id" BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
	"link" TEXT NOT NULL
);;

CREATE TABLE "feed_item" (
	"id" BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
	"channel_id" BIGINT NOT NULL,
	"title" TEXT NOT NULL,
	"author" TEXT,
	"category" TEXT,
	"description" TEXT NOT NULL,
	"guid" TEXT NOT NULL UNIQUE,
	"is_perma_link" TEXT NOT NULL,
	"link" TEXT NOT NULL,
	"pub_date" TEXT NOT NULL
);;

CREATE TABLE "sync_event" (
	"id" BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
	"action" TEXT NOT NULL check (action in ('I','D','U')),
	"feed_item_id" BIGINT NOT NULL
);;

CREATE OR REPLACE FUNCTION sync_function()
	RETURNS TRIGGER
	LANGUAGE PLPGSQL
	AS
$$
BEGIN
	PERFORM pg_notify('sync_event_channel', NULL);
	if (TG_OP = 'INSERT') then
		INSERT INTO sync_event(feed_item_id, action) VALUES (NEW.id, substring(TG_OP,1,1));
		RETURN NEW;
	elsif (TG_OP = 'UPDATE') then
    	INSERT INTO sync_event(feed_item_id, action) VALUES (NEW.id, substring(TG_OP,1,1));
		RETURN NEW;
    elsif (TG_OP = 'DELETE') then
		INSERT INTO sync_event(feed_item_id, action) VALUES (OLD.id, substring(TG_OP,1,1));
		RETURN OLD;
	else
		RAISE WARNING '[sync_function] - unknown action occurred: %, at %', TG_OP, now();
		RETURN NULL;
	end if;
END;
$$;;

CREATE TRIGGER sync_trigger
	AFTER INSERT OR UPDATE OR DELETE
	ON feed_item
	FOR EACH ROW
	EXECUTE PROCEDURE sync_function();;
