package tn.supcom.appsec.controllers;

import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;

public class MessageDispatcher {
    //Paho MQTTv5 Client API
    //all sub subscribe here
    //all publish here
    /*
    * switch(msg_type){
    *   case typ1: publishQualifiedEvent(msg)
    *   default: publishDefaultEvent(msg)
    * }
    * \
    * */


    @Inject
    private Event<JsonObject> eventPublisher;
    //use CDI qualifiers for different publisher for different subscribers (websocket,api,repository)



    public void publishDefaultEvent(JsonObject message) {
        eventPublisher.fireAsync(message);
    }

    public void publishToBroker(String topic, JsonObject message,int qos) {

    }
}
