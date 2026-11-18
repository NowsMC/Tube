package space.nows.tube.server.world.weather;

import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import space.nows.tube.api.world.Weather;

public final class WeatherControl {

    private static final Tag<Task> EXPIRY = Tag.Transient("tube:weather_expiry");

    private WeatherControl() {
    }

    public static Weather current(Instance instance) {
        net.minestom.server.instance.Weather weather = instance.getWeather();
        if (weather.thunderLevel() > 0) {
            return Weather.THUNDER;
        }
        return weather.isRaining() ? Weather.RAIN : Weather.CLEAR;
    }

    public static void apply(Instance instance, Weather weather, int durationTicks) {
        Task pending = instance.getTag(EXPIRY);
        if (pending != null) {
            pending.cancel();
            instance.removeTag(EXPIRY);
        }
        instance.setWeather(engine(weather));
        if (weather != Weather.CLEAR && durationTicks > 0) {
            instance.setTag(EXPIRY, instance.scheduler().buildTask(() -> {
                instance.removeTag(EXPIRY);
                instance.setWeather(engine(Weather.CLEAR));
            }).delay(TaskSchedule.tick(durationTicks)).schedule());
        }
    }

    private static net.minestom.server.instance.Weather engine(Weather weather) {
        return switch (weather) {
            case CLEAR -> net.minestom.server.instance.Weather.CLEAR;
            case RAIN -> net.minestom.server.instance.Weather.RAIN;
            case THUNDER -> net.minestom.server.instance.Weather.THUNDER;
        };
    }
}
