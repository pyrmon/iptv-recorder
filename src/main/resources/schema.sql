CREATE TABLE recording_schedules
(
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    start_time TEXT NOT NULL,
    end_time   TEXT NOT NULL,
    m3u_url    TEXT NOT NULL,
    channel_name TEXT,
    file_name  TEXT NOT NULL,
    triggered  INT  NOT NULL DEFAULT 0
);

CREATE TABLE tvchannel_urls
(
    id     INTEGER PRIMARY KEY AUTOINCREMENT,
    name   TEXT NOT NULL,
    url    TEXT NOT NULL
);