package net.javaci;

import io.grpc.stub.StreamObserver;
import net.javaci.grpc.*;

import java.util.ArrayList;

public class WeatherReporterService extends WeatherReporterGrpc.WeatherReporterImplBase {
    private final ArrayList<CityWeatherData> allWeatherData;

    WeatherReporterService(ArrayList<CityWeatherData> allWeatherData) {
        this.allWeatherData = allWeatherData;
    }

    @Override
    public void getCityWeatherSingleDay(LocationDate request, StreamObserver<CityWeatherData> responseObserver) {
        responseObserver.onNext(checkCityWeatherData(request));
        responseObserver.onCompleted();
    }
    private CityWeatherData checkCityWeatherData(LocationDate request) {
        for (CityWeatherData singleCityWeatherData : allWeatherData) {
            if (singleCityWeatherData.getLocationDate().equals(request)) {
                return singleCityWeatherData;
            }
        }
        // No feature was found, return a CityWeatherData with a 0 for every field.
        return CityWeatherData.getDefaultInstance(); }

    public void getCityWeatherMultipleDays(LocationDatePeriod request, StreamObserver<CityWeatherData> responseObserver) {
        for (CityWeatherData singleCityWeatherData : allWeatherData) {
            if (singleCityWeatherData.getLocationDate().getLocation().equals(request.getLocation())
                    && isWithinDatePeriod(singleCityWeatherData.getLocationDate().getDate(), request)) {
                responseObserver.onNext(singleCityWeatherData);
            } }
        responseObserver.onCompleted();
    }
    private boolean isWithinDatePeriod(Date date, LocationDatePeriod locationDatePeriod) {
        boolean isAfterStartDate = compareDates(date, locationDatePeriod.getStartDate());
        boolean isBeforeEndDate = compareDates(locationDatePeriod.getEndDate(), date);
        return isAfterStartDate && isBeforeEndDate;
    }
    private boolean compareDates(Date date1, Date date2) {
        if (date1.equals(date2)) {
            return true;
        } else if (date1.getYear() > date2.getYear()) {
            return true;
        } else if (date1.getYear() == date2.getYear()) {
            if (date1.getMonth() > date2.getMonth()) {
                return true;
            } else if (date1.getMonth() == date2.getMonth()) {
                return date1.getDay() > date2.getDay();
            } }
        return false;
    }
}