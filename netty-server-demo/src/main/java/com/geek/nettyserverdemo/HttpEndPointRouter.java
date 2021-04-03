package com.geek.nettyserverdemo;

import java.util.List;

public interface HttpEndPointRouter {

    String route(List<String> endpoints);
}
