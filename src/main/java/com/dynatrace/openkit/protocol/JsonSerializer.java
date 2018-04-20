package com.dynatrace.openkit.protocol;

import java.util.Arrays;

import javax.print.event.PrintEvent;

import com.dynatrace.openkit.protocol.dto.Action;
import com.dynatrace.openkit.protocol.dto.Device;
import com.dynatrace.openkit.protocol.dto.Payload;
import com.dynatrace.openkit.protocol.dto.Session;

public class JsonSerializer implements Serializer {
	@Override
	public byte[] serialize(Payload data) {
		StringBuilder builder = new StringBuilder();

		//        var payload = {
		//                timestamp: new Date().getTime(),
		//                sessionStart: this.startTs,
		//                endpoint: "Dynatrace",
		//                agent: "DT OpenAgent",
		//                version: "0.1",
		builder.append("{")
				.append("\"timestamp\": ").append(System.currentTimeMillis() / 1000).append(",")
				.append("\"sessionStart\": ").append(data.getSession().getStartTime()).append(",")
				.append("\"endpoint\": \"Dynatrace\",")
				.append("\"agent\": \"DT OpenAgent\",")
				.append("\"version\": \"0.1\",")
				//                device: {
				//        },
				.append("\"device\": ").append(serializeDevice(data.getDevice())).append(",")
				//        session: {
				//        },
				.append("\"session\": ").append(serializeSession(data.getSession())).append(",")
				//        entities: [],
				.append("\"entities\": [],")
		;
		//        actions: []
		//        };

		builder.append("\"actions\": [");
		//        for (i in actionStack) {
		for (int i = 0; i < data.getActions().size(); i++) {
			Action action = data.getActions().get(i);
			if (i > 0) {
				builder.append(",");
			}
			builder.append(serializeAction(action, data.getSession()));
		}
		builder.append("]");
		return builder.append("}").toString().getBytes();
	}

	private String serializeDevice(Device device) {
		//            id:             this.deviceEntity.id,
		//                    name:           this.deviceEntity.name,
		//                    clientIP:       this.deviceEntity.ip,
		//                    cpu:            this.deviceEntity.cpu,
		//                    os:             this.deviceEntity.os,
		//                    freemem:        this.deviceEntity.freemem,
		//                    totalmem:       this.deviceEntity.totalmem,
		//                    batteryLevel:   this.deviceEntity.batteryLevel,
		//                    manufacturer:   this.deviceEntity.manufacturer,
		//                    modelId:        this.deviceEntity.modelId,
		//                    appVersion:     this.deviceEntity.appVersion,
		//                    appBuild:       this.deviceEntity.appBuild

		String builder = "{" + "\"id\":\"" + device.getVisitorId() + "\","
				+ "\"clientIP\":\"" + device.getClientIPAddress() + "\""
				+ "}";
		return builder;
	}

	private String serializeSession(Session session) {
		//            id: this.sessionId,
		//                    name: this.deviceEntity.name,
		//                    start: this.startTs,

		String builder = "{" + "\"id\":" + session.getId() + ","
				+ "\"start\":" + session.getStartTime()
				+ "}";
		return builder;
	}

	private String serializeAction(Action action, Session session) {

		//            var entry = {
		//                    name:               action.name,
		//                    type:               action.type,
		//                    id:                 action.id,
		//                    parent:             (action.parent ? action.parent.id : 0),
		//                    s0:                 action.s0,
		//                    start:              action.startTs,
		//                    t0:                 action.startTs - this.startTs,
		//                    s1:                 action.s1,
		//                    end:                action.endTs,
		//                    t1:                 action.endTs - action.startTs
		//            };

		// TODO: implement
		//            if (action.type == 'VALUE') {
		//                entry.value = action.options.value;
		//            };
		//            if (action.type == 'WEBREQUEST') {
		//                entry.network = {
		//                        responseCode: action.options.responseCode,
		//                        bytesSent: action.options.bytesSent,
		//                        bytesReceived: action.options.bytesReceived
		//                };
		//            };
		//            if (action.type == 'ERROR' || action.type == 'CRASH') {
		//                entry.error = {
		//                        reason:             action.options.reason,
		//                        description:        action.options.description,
		//                        stacktrace:         action.options.stacktrace
		//                };
		//            };
		//            payload.actions.push(entry);
		//        }

		String builder = "{" + "\"name\":\"" + action.getName() + "\","
				+ "\"type\":\"" + action.getEventType() + "\","
				+ "\"id\":" + action.getActionId() + ","
				+ "\"parent\":" + action.getParentActionId() + ","
				+ "\"s0\":" + action.getStartSequenceNumber() + ","
				+ "\"start\":" + action.getStartTime() + ","
				+ "\"t0\":" + (action.getStartTime() - session.getStartTime()) + ","
				+ "\"s1\":" + action.getEndSequenceNumber() + ","
				+ "\"end\":" + action.getEndTime() + ","
				+ "\"t1\":" + (action.getEndTime() - session.getStartTime())
				+ "}";
		return builder;
	}

	public static void main(String[] args) {
		Payload data = new Payload("1.2.3.4", 1, System.currentTimeMillis(), "Visitor1");

		Action action = new Action();
		action.setActionId(1);
		action.setName("Action 1");
		action.setEventType(EventType.ACTION);
		action.setStartSequenceNumber(1);
		action.setStartTime(System.currentTimeMillis()+ 3);
		action.setEndSequenceNumber(2);
		action.setEndTime(System.currentTimeMillis() + 7);
		action.setName("Action 1");
		data.getActions().add(action);

		System.out.println(new String(new JsonSerializer().serialize(data)));
	}
}
