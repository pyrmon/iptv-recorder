package me.schickel.recorder.util;

import me.schickel.recorder.entity.RecordingSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class TimeUtilsTest {

    private TimeUtils timeUtils;

    @BeforeEach
    void setUp() {
        timeUtils = new TimeUtils();
    }

    @Test
    void isBefore_shouldReturnTrue_whenStartDateIsBeforeEndDate() {
        String startDate = "10:00 01/01/2024";
        String endDate = "11:00 01/01/2024";

        boolean result = timeUtils.isBefore(startDate, endDate);

        assertThat(result).isTrue();
    }

    @Test
    void isBefore_shouldReturnFalse_whenStartDateIsAfterEndDate() {
        String startDate = "11:00 01/01/2024";
        String endDate = "10:00 01/01/2024";

        boolean result = timeUtils.isBefore(startDate, endDate);

        assertThat(result).isFalse();
    }

    @Test
    void isBefore_shouldReturnFalse_whenStartDateEqualsEndDate() {
        String startDate = "10:00 01/01/2024";
        String endDate = "10:00 01/01/2024";

        boolean result = timeUtils.isBefore(startDate, endDate);

        assertThat(result).isFalse();
    }

    @Test
    void isBefore_shouldReturnFalse_whenStartDateIsInvalid() {
        String startDate = "invalid-date";
        String endDate = "10:00 01/01/2024";

        boolean result = timeUtils.isBefore(startDate, endDate);

        assertThat(result).isFalse();
    }

    @Test
    void isBefore_shouldReturnFalse_whenEndDateIsInvalid() {
        String startDate = "10:00 01/01/2024";
        String endDate = "invalid-date";

        boolean result = timeUtils.isBefore(startDate, endDate);

        assertThat(result).isFalse();
    }

    @Test
    void isInPast_shouldReturnTrue_whenDateIsInPast() {
        String pastDate = "10:00 01/01/2020";

        boolean result = timeUtils.isInPast(pastDate);

        assertThat(result).isTrue();
    }

    @Test
    void isInPast_shouldReturnFalse_whenDateIsInFuture() {
        String futureDate = "10:00 01/01/2030";

        boolean result = timeUtils.isInPast(futureDate);

        assertThat(result).isFalse();
    }

    @Test
    void isInPast_shouldReturnFalse_whenDateIsInvalid() {
        String invalidDate = "invalid-date";

        boolean result = timeUtils.isInPast(invalidDate);

        assertThat(result).isFalse();
    }

    @Test
    void parseStringToLocalDateTime_shouldParseValidDateString() {
        String timeString = "14:30 15/03/2024";

        LocalDateTime result = timeUtils.parseStringToLocalDateTime(timeString);

        assertThat(result).isEqualTo(LocalDateTime.of(2024, 3, 15, 14, 30));
    }

    @Test
    void calculateTimeToRecord_shouldReturnPositiveSeconds_whenEndTimeIsInFuture() {
        LocalDateTime futureTime = LocalDateTime.now().plusHours(1);

        String result = timeUtils.calculateTimeToRecord(futureTime);

        long seconds = Long.parseLong(result);
        assertThat(seconds).isPositive();
        assertThat(seconds).isCloseTo(3600, within(10L));
    }

    @Test
    void calculateTimeToRecord_shouldReturnNegativeSeconds_whenEndTimeIsInPast() {
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);

        String result = timeUtils.calculateTimeToRecord(pastTime);

        long seconds = Long.parseLong(result);
        assertThat(seconds).isNegative();
        assertThat(seconds).isCloseTo(-3600, within(10L));
    }

    @Test
    void compare_shouldReturnNegative_whenFirstScheduleStartsEarlier() {
        RecordingSchedule schedule1 = createSchedule("10:00 01/01/2024");
        RecordingSchedule schedule2 = createSchedule("11:00 01/01/2024");

        int result = timeUtils.compare(schedule1, schedule2);

        assertThat(result).isNegative();
    }

    @Test
    void compare_shouldReturnPositive_whenFirstScheduleStartsLater() {
        RecordingSchedule schedule1 = createSchedule("11:00 01/01/2024");
        RecordingSchedule schedule2 = createSchedule("10:00 01/01/2024");

        int result = timeUtils.compare(schedule1, schedule2);

        assertThat(result).isPositive();
    }

    @Test
    void compare_shouldReturnZero_whenBothSchedulesStartAtSameTime() {
        RecordingSchedule schedule1 = createSchedule("10:00 01/01/2024");
        RecordingSchedule schedule2 = createSchedule("10:00 01/01/2024");

        int result = timeUtils.compare(schedule1, schedule2);

        assertThat(result).isZero();
    }

    private RecordingSchedule createSchedule(String startTime) {
        RecordingSchedule schedule = new RecordingSchedule();
        schedule.setStartTime(startTime);
        return schedule;
    }
}