package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties;

import org.eclipse.bpmn2.CallActivity;
import org.eclipse.emf.ecore.util.FeatureMap;

public class CallActivityPropertyWriter extends ActivityPropertyWriter {

    private final CallActivity activity;

    public CallActivityPropertyWriter(CallActivity activity) {
        super(activity);
        this.activity = activity;
    }

    public void setIndependent(Boolean independent) {
        FeatureMap.Entry value = attribute("independent", independent);
        this.activity.getAnyAttribute().add(value);
    }

    public void setWaitForCompletion(Boolean waitForCompletion) {
        FeatureMap.Entry value = attribute("waitForCompletion", waitForCompletion);
        this.activity.getAnyAttribute().add(value);
    }

    public void setAsync(Boolean async) {
        setMeta("customAsync", String.valueOf(async));
    }

    public void setCalledElement(String value) {
        activity.setCalledElement(value);
    }
}
