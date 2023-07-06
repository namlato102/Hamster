package de.hsrm.cs.wwwvs.hamster.iot;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class SimulatedHamster {
    private Consumer<Integer> _roundsCallback;
    private int _rounds;
    private TimerTask _timerTask;
    private Timer _timer;

    private String _hamsterId;

    public SimulatedHamster(String hamsterId) {
        _hamsterId = hamsterId;
    }

    public String getHamsterId() {
        return _hamsterId;
    }

    public void fondle(int amount) {
        System.out.println(String.format("%d fondles received for Hamster %s", amount, _hamsterId));
    }

    public void punish(int amount) {
        System.out.println(String.format("%d punishments received for Hamster %s", amount, _hamsterId));
    }

    public void startRunning() {
        stopRunning();
        _timerTask = new TimerTask() {
            @Override
            public void run() {
                _rounds++;
                if (_roundsCallback != null) {
                    _roundsCallback.accept(_rounds);
                }
            }
        };
        if (_timer == null) {
            _timer = new Timer();
            _timer.schedule(_timerTask, 100, 100);
        }
    }

    public void stopRunning() {
        if (_timerTask != null) {
            _timerTask.cancel();
            _timerTask = null;
        }
        if (_timer != null) {
            _timer.cancel();
            _timer = null;
        }
    }

    public void setRevolutionCallback(Consumer<Integer> callback) {
        _roundsCallback = callback;
    }
}
