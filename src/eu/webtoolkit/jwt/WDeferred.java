package eu.webtoolkit.jwt;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WWidget;

public class WDeferred <T extends WWidget>  extends WContainerWidget {

	private Class<T> cl;
	private Object[] parameterValues;

	public WDeferred(Class<T> cl, Object... parameterValues) {
		this.cl = cl;
		this.parameterValues = parameterValues;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void load() {
		if (getCount() == 0) {
			try {
				Constructor<T>[] constructors = (Constructor<T>[]) cl.getConstructors();
				for (int i = 0; i < constructors.length; ++i) {
					if (constructors[i].getParameterTypes().length == parameterValues.length) {
						WWidget w = constructors[i].newInstance(parameterValues);
						addWidget(w);
						break;
					}
				}
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}	
		}
		
		super.load();
	}
}
