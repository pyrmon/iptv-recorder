package me.schickel.recorder.util;

import me.schickel.recorder.entity.RecordingSchedule;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;

@Component
public class TimeUtils implements Comparator<RecordingSchedule> {

    private static final String DATE_TIME_FORMAT = "HH:mm dd/MM/yyyy";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    private final SimpleDateFormat simpleDateFormatter = new SimpleDateFormat(DATE_TIME_FORMAT);

    public boolean isBefore(String startDate, String endDate) {
        try {
            simpleDateFormatter.setLenient(false); // Set to strictly enforce format
            Date start = simpleDateFormatter.parse(startDate);
            Date end = simpleDateFormatter.parse(endDate);
            return start.before(end);
        } catch (ParseException e) {
            return false; // Handle invalid date formats as being not before
        }
    }

    public boolean isInPast(String date) {
        try {
            simpleDateFormatter.setLenient(false); // Set to strictly enforce format
            Date dateToCheck = simpleDateFormatter.parse(date);
            return dateToCheck.before(new Date());
        } catch (ParseException e) {
            return false; // Handle invalid date formats as being not in the past
        }
    }

    public LocalDateTime parseStringToLocalDateTime(String timeString) {
        return LocalDateTime.parse(timeString, formatter);
    }

    public String calculateTimeToRecord(LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();
        long secondsBetween = java.time.Duration.between(now, endTime).getSeconds();
        return String.valueOf(secondsBetween);
    }

    public String parseLocalDateTimeToString(LocalDateTime dateTime) {
        return dateTime.format(formatter);
    }

    @Override
    public int compare(RecordingSchedule o1, RecordingSchedule o2) {
        LocalDateTime startTime1 = LocalDateTime.parse(o1.getStartTime(), formatter);
        LocalDateTime startTime2 = LocalDateTime.parse(o2.getStartTime(), formatter);
        return startTime1.compareTo(startTime2);
    }
}
