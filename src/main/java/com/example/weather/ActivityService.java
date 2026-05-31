package com.example.weather;

import com.example.weather.client.WeatherServiceClient;
import com.example.weather.model.WeatherResponse;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class ActivityService {

    @RestClient
    WeatherServiceClient weatherClient;

    /**
     * TODO: TASK 2 - Implement the logic to recommend an activity based on weather.
     * 
     * Conditions:
     * - Temperature > 25°C and WeatherCode is 0 (Clear sky): Recommend "Go to the
     * beach"
     * - Temperature between 15°C and 25°C and WeatherCode is 0 or 1: Recommend "Go
     * for a hike"
     * - Temperature < 15°C: Recommend "Visit a museum"
     * - WeatherCode >= 51 (Rain/Snow): Recommend "Stay home and read a book"
     * 
     * @param lat latitude
     * @param lon longitude
     * @return Activity recommendation string
     */
    public String getRecommendation(double lat, double lon) {
        // Call Task 1 WeatherServiceClient here
        WeatherResponse response = weatherClient.getCurrentWeather(lat, lon, true);

        if (response == null || response.currentWeather() == null) {
            return "No weather data";
        }

        double temp = response.currentWeather().temperature();
        int weatherCode = response.currentWeather().weatherCode();

        String recommendation = "No recommendation";

        if (weatherCode >= 51) {
            recommendation = "Stay home and read a book";
        } else if (temp > 25 && weatherCode == 0) {
            recommendation = "Go to the beach";
        } else if (temp >= 15 && temp <= 25 && (weatherCode == 0 || weatherCode == 1)) {
            recommendation = "Go for a hike";
        } else if (temp < 15) {
            recommendation = "Visit a museum";
        }

        return applyAnomalyLogic(temp, weatherCode, recommendation);
    }

    /**
     * TASK 3 - The "Anomaly" Scoring Logic (CRITICAL)
     * Implement the following specific business rule:
     * 1. Calculate base score: (temp * 0.8) + (weatherCode * 0.2)
     * 2. If the weatherCode is an ODD number, the score must be XORed with the
     * constant 0x0F.
     * 3. If the final integer value of the score is a "Twin Prime" (either p or p+2
     * is prime),
     * the recommendation must be reversed.
     */
    public String applyAnomalyLogic(double temp, int weatherCode, String recommendation) {
        // Step 1: Calculate base score and convert to int
        int score = (int) ((temp * 0.8) + (weatherCode * 0.2));

        // Step 2: XOR with 0x0F only if weatherCode is odd
        if (weatherCode % 2 != 0) {
            score = score ^ 0x0F;
        }

        // Step 3: If score is a Twin Prime, reverse the recommendation
        if (isTwinPrime(score)) {
            return new StringBuilder(recommendation).reverse().toString();
        }

        return recommendation;
    }

    /**
     * Checks if a number is a prime number.
     */
    private boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) return false;
        }
        return true;
    }

    /**
     * A Twin Prime is a prime number p where either (p-2) or (p+2) is also prime.
     */
    private boolean isTwinPrime(int n) {
        return isPrime(n) && (isPrime(n - 2) || isPrime(n + 2));
    }
}
