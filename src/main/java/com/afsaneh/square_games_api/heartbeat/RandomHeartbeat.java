package com.afsaneh.square_games_api.heartbeat;

import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class RandomHeartbeat implements HeartbeatSensor {

    @Override
    public int get() {
        return ThreadLocalRandom.current().nextInt(40, 231);
    }

}
