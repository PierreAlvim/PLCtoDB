package br.com.zecon.log;

import java.time.LocalDateTime;

public abstract class LogEvent {
    LocalDateTime time;
    EventTypes type;
    String obs;

    public enum EventTypes {
        OK("SUCCESS"), ERROR("ERROR"), WARN("WARNING"), INFO("INFO");

        public String text;
        EventTypes(String t) {
            text = t;
        }
    }
}
