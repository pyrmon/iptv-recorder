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

CREATE TABLE past_recordings
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    channel_name TEXT,
    m3u_url      TEXT NOT NULL,
    file_name    TEXT NOT NULL,
    start_time   TEXT NOT NULL,
    end_time     TEXT NOT NULL,
    recorded_at  TEXT NOT NULL,
    was_triggered INT NOT NULL DEFAULT 0
);