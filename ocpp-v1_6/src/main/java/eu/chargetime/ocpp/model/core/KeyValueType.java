package eu.chargetime.ocpp.model.core;

import eu.chargetime.ocpp.PropertyConstraintException;
import eu.chargetime.ocpp.model.Validatable;
import eu.chargetime.ocpp.utilities.ModelUtil;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/*
 ChargeTime.eu - Java-OCA-OCPP
 Copyright (C) 2015-2016 Thomas Volden <tv@chargetime.eu>

 MIT License

 Copyright (C) 2016-2018 Thomas Volden

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

/**
 * Contains information about a specific configuration key.
 * It is returned in {@link GetConfigurationConfirmation}.
 */
@XmlRootElement
@XmlType(propOrder = {"key", "readonly", "value"})
public class KeyValueType implements Validatable {
    private String key;
    private Boolean readonly;
    private String value;

    /**
     * Name of the key.
     *
     * @return key.
     */
    public String getKey() {
        return key;
    }

    /**
     * Required. Name of the key.
     *
     * @param key String, max 50 characters, case insensitive.
     * @throws PropertyConstraintException Value exceeds 50 charactesr.
     */
    @XmlElement
    public void setKey(String key) throws PropertyConstraintException{
        if (!isValidKey(key))
            throw new PropertyConstraintException("key", key);

        this.key = key;
    }

    private boolean isValidKey(String key) {
        return ModelUtil.validate(key, 50);
    }

    /**
     * False if the value can be set with a {@link ChangeConfigurationRequest}.
     *
     * @return Is configuration read only.
     */
    public Boolean getReadonly() {
        return readonly;
    }

    /**
     * Required. False if the value can be set with a {@link ChangeConfigurationRequest}.
     *
     * @param readonly Boolean, configuration is read only.
     * @throws PropertyConstraintException Value was null.
     */
    @XmlElement
    public void setReadonly(Boolean readonly) throws PropertyConstraintException {
        if (!isValidReadonly(readonly))
            throw new PropertyConstraintException("readonly", readonly);

        this.readonly = readonly;
    }

    private boolean isValidReadonly(Boolean readonly) {
        return readonly != null;
    }

    /**
     * If key is known but not set, this field may be absent.
     *
     * @return Value associated to the key.
     */
    public String getValue() {
        return value;
    }

    /**
     * Optional. If key is known but not set, this field may be absent.
     *
     * @param value                         String, max 500 characters, case insensitive.
     * @throws PropertyConstraintException  Value exceeds 500 characters.
     */
    @XmlElement
    public void setValue(String value) throws PropertyConstraintException {
        if (!isValidValue(value))
            throw new PropertyConstraintException("value", value);

        this.value = value;
    }

    private boolean isValidValue(String value) {
        return ModelUtil.validate(value, 500);
    }

    @Override
    public boolean validate() {
        boolean output = true;
        output &= isValidKey(this.key);
        output &= isValidReadonly(this.readonly);
        return output;
    }
}
