CREATE TABLE "FEED" (
	"ID" BIGINT PRIMARY KEY AUTO_INCREMENT,
	"CHANNEL_ID" BIGINT NOT NULL,
	"URL" TEXT NOT NULL,
	"ETAG" TEXT,
	"LAST_MODIFIED" TEXT
);