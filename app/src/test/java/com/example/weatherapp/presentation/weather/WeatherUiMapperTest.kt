package com.example.weatherapp.presentation.weather

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.example.weatherapp.domain.model.CurrentWeather
import com.example.weatherapp.domain.model.HourlyForecast
import com.example.weatherapp.domain.model.WeatherCondition
import com.example.weatherapp.domain.model.WeatherConditionType
import com.example.weatherapp.testutil.TestData
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale
import java.util.TimeZone

/**
 * Tests for [CityWeather.toUi] — the single mapping every rendered pixel passes through.
 *
 * `toUi` formats via `SimpleDateFormat` with the JVM's default locale and time zone, so both
 * are pinned here; without that these assertions pass or fail depending on where the machine
 * running them happens to be.
 */
class WeatherUiMapperTest {

    private lateinit var originalTimeZone: TimeZone
    private lateinit var originalLocale: Locale

    @BeforeEach
    fun setUp() {
        originalTimeZone = TimeZone.getDefault()
        originalLocale = Locale.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        Locale.setDefault(Locale.US)
    }

    @AfterEach
    fun tearDown() {
        TimeZone.setDefault(originalTimeZone)
        Locale.setDefault(originalLocale)
    }

    /** 2023-11-14 22:00:00 UTC — aligned to the hour so time labels read cleanly. */
    private val base = 1_699_999_200L

    private fun hoursFrom(start: Long, count: Int) = List(count) { i ->
        HourlyForecast(
            timeEpoch = start + i * 3600,
            tempC = 15.0,
            condition = WeatherCondition(1000, "Sunny", isDay = true)
        )
    }

    // --- Core field mapping ---

    @Test
    fun `maps the headline display fields`() {
        val weather = TestData.cityWeather(
            city = TestData.city(id = "london", name = "London"),
            current = TestData.current(tempC = 18.0, code = 1000, isDay = true),
            daily = TestData.daily(count = 3, maxTempC = 22.0, minTempC = 14.0)
        )

        val ui = weather.toUi(nowEpochSeconds = base)

        assertThat(ui.cityId).isEqualTo("london")
        assertThat(ui.cityName).isEqualTo("London")
        assertThat(ui.temperature).isEqualTo("18°")
        assertThat(ui.conditionText).isEqualTo("Sunny")
        assertThat(ui.conditionType).isEqualTo(WeatherConditionType.CLEAR)
        assertThat(ui.isDay).isTrue()
        assertThat(ui.highLow).isEqualTo("H:22°   L:14°")
        // TestData.current() stamps lastUpdatedEpoch = 1_700_000_000 (22:13 UTC).
        assertThat(ui.lastUpdated).isEqualTo("Updated 22:13")
    }

    @Test
    fun `condition code drives the theming type`() {
        val thunder = TestData.cityWeather(current = TestData.current(code = 1276))
        assertThat(thunder.toUi(base).conditionType).isEqualTo(WeatherConditionType.THUNDER)

        val unknownCode = TestData.cityWeather(current = TestData.current(code = 9999))
        assertThat(unknownCode.toUi(base).conditionType).isEqualTo(WeatherConditionType.CLOUDY)
    }

    @Test
    fun `night conditions are flagged for the palette`() {
        val night = TestData.cityWeather(current = TestData.current(isDay = false))
        assertThat(night.toUi(base).isDay).isFalse()
    }

    // --- Temperature formatting ---

    @Test
    fun `temperatures round to the nearest whole degree`() {
        fun tempFor(c: Double) = TestData.cityWeather(
            current = CurrentWeather(
                tempC = c,
                condition = WeatherCondition(1000, "Sunny", isDay = true),
                lastUpdatedEpoch = base
            )
        ).toUi(base).temperature

        assertThat(tempFor(18.6)).isEqualTo("19°")
        assertThat(tempFor(18.4)).isEqualTo("18°")
        assertThat(tempFor(-3.7)).isEqualTo("-4°")
        assertThat(tempFor(0.0)).isEqualTo("0°")
    }

    // --- Hourly slicing ---

    @Test
    fun `hourly is capped at 24 entries`() {
        val weather = TestData.cityWeather(hourly = hoursFrom(base, count = 48))

        val ui = weather.toUi(nowEpochSeconds = base)

        assertThat(ui.hourly).hasSize(24)
        assertThat(ui.hourly.first().timeLabel).isEqualTo("22:00")
    }

    @Test
    fun `hourly starts from the upcoming hours, not the start of the cached day`() {
        // Cache holds 48 hours from `base`; "now" is 10 hours in.
        val weather = TestData.cityWeather(hourly = hoursFrom(base, count = 48))

        val ui = weather.toUi(nowEpochSeconds = base + 10 * 3600)

        // The one-hour grace window means the 09:00 slot (now - 1h) is still shown.
        assertThat(ui.hourly).hasSize(24)
        assertThat(ui.hourly.first().timeLabel).isEqualTo("07:00")
    }

    @Test
    fun `the grace window keeps exactly one elapsed hour`() {
        val weather = TestData.cityWeather(hourly = hoursFrom(base, count = 5))

        // now = base + 2h, so the threshold falls exactly on the base + 1h slot.
        val ui = weather.toUi(nowEpochSeconds = base + 2 * 3600)

        assertThat(ui.hourly).hasSize(4)
        assertThat(ui.hourly.first().timeLabel).isEqualTo("23:00")
    }

    @Test
    fun `hourly is empty when every cached hour has elapsed`() {
        val weather = TestData.cityWeather(hourly = hoursFrom(base, count = 6))

        val ui = weather.toUi(nowEpochSeconds = base + 48 * 3600)

        assertThat(ui.hourly).isEmpty()
    }

    @Test
    fun `no cached hours maps to an empty strip`() {
        val weather = TestData.cityWeather(hourly = emptyList())
        assertThat(weather.toUi(base).hourly).isEmpty()
    }

    // --- Daily labelling ---

    @Test
    fun `the first day is labelled Today and the rest use weekday names`() {
        val weather = TestData.cityWeather(daily = TestData.daily(count = 3))

        val ui = weather.toUi(base)

        // TestData.daily() starts at 1_700_000_000 (Tue 14 Nov 2023) and steps one day.
        assertThat(ui.daily.map { it.dayLabel }).isEqualTo(listOf("Today", "Wed", "Thu"))
    }

    @Test
    fun `daily highs and lows are formatted per day`() {
        val weather = TestData.cityWeather(
            daily = TestData.daily(count = 2, maxTempC = 21.5, minTempC = 9.2)
        )

        val first = weather.toUi(base).daily.first()

        assertThat(first.high).isEqualTo("22°")
        assertThat(first.low).isEqualTo("9°")
    }

    @Test
    fun `no cached days leaves the high-low summary blank`() {
        val weather = TestData.cityWeather(daily = emptyList())

        val ui = weather.toUi(base)

        assertThat(ui.daily).isEmpty()
        assertThat(ui.highLow).isEqualTo("")
    }

    // --- Name and region resolution ---

    @Test
    fun `the auto-detected city shows as My Location with its resolved name beneath`() {
        val weather = TestData.cityWeather(
            city = TestData.city(name = "Bengaluru", isCurrentLocation = true)
        )

        val ui = weather.toUi(base)

        assertThat(ui.cityName).isEqualTo("My Location")
        assertThat(ui.region).isEqualTo("Bengaluru")
    }

    @Test
    fun `a saved city shows its own name and region`() {
        val weather = TestData.cityWeather(
            city = TestData.city(name = "London", isCurrentLocation = false)
        )

        val ui = weather.toUi(base)

        assertThat(ui.cityName).isEqualTo("London")
        assertThat(ui.region).isEqualTo("England")
    }

    @Test
    fun `region falls back to country when the API returns no region`() {
        val weather = TestData.cityWeather(
            city = TestData.city(name = "Singapore").copy(region = "", country = "Singapore")
        )

        assertThat(weather.toUi(base).region).isEqualTo("Singapore")
    }
}
