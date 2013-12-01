package com.leepapesweers.flashnotifier;

import android.text.TextUtils;
import android.widget.EditText;

import com.andreabaccega.formedittextvalidator.Validator;

public class PatternValidator extends Validator {

    public PatternValidator() {
        super("Not a valid pattern!");
    }

    public boolean isValid(EditText et) {



        return TextUtils.equals(et.getText(), "ciao");
    }

}