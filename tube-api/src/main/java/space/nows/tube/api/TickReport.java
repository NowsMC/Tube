package space.nows.tube.api;

public record TickReport(double lastFiveSeconds, double lastMinute, double lastFiveMinutes, double averageMillis) {
}
