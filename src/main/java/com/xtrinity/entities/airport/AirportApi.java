package com.xtrinity.entities.airport;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;

public class AirportApi {
    public static Method[] reachGetters() {
        return Arrays.stream(Airport.class.getDeclaredMethods())
                .filter(m -> m.getName().startsWith("get"))
                .sorted(AirportApi::methodsComparator)
                .toArray(Method[]::new);
    }

    public static Method[] reachSetters() {
        return Arrays.stream(Airport.class.getDeclaredMethods())
                .filter(m -> m.getName().startsWith("set"))
                .sorted(AirportApi::methodsComparator)
                .toArray(Method[]::new);
    }

    private static int methodsComparator(Method method, Method method1) {
        Function<Method, Integer> getColN = m -> Integer.parseInt(m.getName().replaceAll("\\D", ""));
        return Integer.compare(getColN.apply(method), getColN.apply(method1));
    }
}
