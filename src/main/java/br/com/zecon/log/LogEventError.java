package br.com.zecon.log;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @AllArgsConstructor
public class LogEventError extends LogEvent {
    Exception exception;

    public LogEventError(Exception exception, String obs)
    {
        this.type = EventTypes.ERROR;
        this.exception = exception;
        this.obs = obs;
        this.time = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "=== " + time.toString() + " ===\n" + type.text + ":" + (exception==null?"":exception) + "\n" + obs + "\n------------------";
    }


}
