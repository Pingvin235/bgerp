package ru.bgcrm.model.param.address;

import ru.bgcrm.model.param.ParameterSearchedObject;

/**
 * Устаревший объект, оставлен для совместимости с JSP.
 * @param <T>
 */
@Deprecated
public class AddressSearchedObject<T> extends ParameterSearchedObject<T> {
	
	public AddressSearchedObject(T object, int paramId, Object value) {
		super(object, paramId, value);
	}

	public String getParamName() {
		return param == null ? null : param.getTitle();
	}

	public String getAddress() {
		return (String) value;
	}

}
