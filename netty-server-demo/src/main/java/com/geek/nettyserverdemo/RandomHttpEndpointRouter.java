package com.geek.nettyserverdemo;

import java.util.List;
import java.util.Random;

public class RandomHttpEndpointRouter implements HttpEndPointRouter {
    public String route(List<String> endpoints) {
        Random random = new Random(System.currentTimeMillis());
        return endpoints.get(random.nextInt(endpoints.size()));
    }
}
