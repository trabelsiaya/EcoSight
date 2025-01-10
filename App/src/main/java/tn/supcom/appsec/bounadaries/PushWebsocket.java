package tn.supcom.appsec.bounadaries;

import jakarta.enterprise.event.ObservesAsync;
import jakarta.json.JsonObject;

public class PushWebsocket {

    private void notifyAll(JsonObject obj){

    }

    public void onMessageFromDispatcher(@ObservesAsync JsonObject message) {
        notifyAll(message);
    }
}
