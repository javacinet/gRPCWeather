package net.javaci;

import net.javaci.grpc.*;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class WeatherReporterServer {
    private static final Logger logger = Logger.getLogger(WeatherReporterServer.class.getName());

    private final int port;
    private final Server server;

    /**
     * Create a WeatherReporterServer server listening on {@code port} using {@code cityWeatherData}.
     */
    public WeatherReporterServer(int port, ArrayList<CityWeatherData> cityWeatherData) throws IOException {
        this(Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create()),
                port, cityWeatherData);
    }

    /**
     * Create a WeatherReporterServer server using serverBuilder as a base
     */
    public WeatherReporterServer(ServerBuilder<?> serverBuilder, int port, ArrayList<CityWeatherData> cityWeatherData) {
        this.port = port;
        server = serverBuilder.addService(new WeatherReporterService(cityWeatherData)).build();
    }

    /**
     * Start serving requests.
     */
    public void start() throws IOException {
        server.start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    WeatherReporterServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    /**
     * Stop serving requests and shutdown resources.
     */
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws Exception {
        ArrayList<CityWeatherData> cityWeatherData = createCityWeatherData();
        WeatherReporterServer server = new WeatherReporterServer(8980, cityWeatherData);
        server.start();
        server.blockUntilShutdown();
    }

    private static ArrayList<CityWeatherData> createCityWeatherData() {
        ArrayList<CityWeatherData> cityWeatherData = new ArrayList<>();
        Weather weather = Weather.newBuilder()
                .setTemperature(10)
                .setHumidity(30)
                .setWind(25)
                .build();

        Location location = Location.newBuilder()
                .setCity("Ankara")
                .setCountry("Turkiye")
                .build();

        Date date = Date.newBuilder()
                .setYear(2024)
                .setMonth(8)
                .setDay(30)
                .build();

        LocationDate locationDate = LocationDate.newBuilder()
                .setDate(date)
                .setLocation(location)
                .build();

        cityWeatherData.add(CityWeatherData
                .newBuilder()
                .setWeather(weather)
                .setLocationDate(locationDate)
                .build());
        return cityWeatherData;
    }

}

    