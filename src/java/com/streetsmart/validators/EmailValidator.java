/*
 * Created by Hung Vu on 2016.04.07  * 
 * Copyright Â© 2016 Hung Vu. All rights reserved. * 
 */
package com.streetsmart.validators;


import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("emailValidator")
/**
 *
 * @author Kevin
 */
public class EmailValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        
        // Typecast the password "value" entered by the user to String.
        String email = (String) value;

        if (email == null || email.isEmpty()) {
            // Do not take any action. 
            // The required="true" in the XHTML file will catch this and produce an error message.
            return;
        }
        
        // Validate if the email address entered by the user is in the right format.
        String regex = "([^.@]+)(\\.[^.@]+)*@([^.@]+\\.)+([^.@]+)";
        if (!email.matches(regex)) {
            throw new ValidatorException(new FacesMessage("Email is not a valid address."));
        }        
    } 
    
}