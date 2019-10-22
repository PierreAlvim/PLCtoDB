package br.com.zecon.log;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @AllArgsConstructor
public class LogEventInfo extends LogEvent {

    public LogEventInfo(EventTypes type, String obs)
    {
        this.type = type;
        this.obs = obs;
        this.time = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "=== " + time.toString() + " ===\n" + type.text + ":\n" + obs + "\n------------------";
    }


}
