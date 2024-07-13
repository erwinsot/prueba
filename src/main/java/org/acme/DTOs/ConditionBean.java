package org.acme.DTOs;


import jakarta.enterprise.context.ApplicationScoped;

/**
 * This class is used to store the condition to apply the transformation or not.
 */

@ApplicationScoped
public class ConditionBean {    private boolean applyTransformation = false;

    /**
     * This method is used to get the value of the applyTransformation flag.
     *
     * @return the value of the applyTransformation flag
     */

    public boolean isApplyTransformation() {
        return applyTransformation;
    }

    /**
     * This method is used to set the value of the applyTransformation flag.
     *
     * @param applyTransformation the new value for the applyTransformation flag
     */

    public void setApplyTransformation(boolean applyTransformation) {
        this.applyTransformation = applyTransformation;
    }

}
