package eu.webtoolkit.jwt;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WWidget;

public class WDeferred <T extends WWidget>  extends WContainerWidget {
	private static final Logger logger = LoggerFactory.getLogger(WDeferred.class);
	
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
				logger.error("InstantiationException {}", cl.getName(), e);
			} catch (IllegalAccessException e) {
				logger.error("IllegalAccessException {}", cl.getName(), e);
			} catch (IllegalArgumentException e) {
				logger.error("IllegalArgumentException {}", cl.getName(), e);
			} catch (InvocationTargetException e) {
				logger.error("InvocationTargetException {}", cl.getName(), e);
			} catch (SecurityException e) {
				logger.error("SecurityException {}", cl.getName(), e);
			}	
		}
		
		super.load();
	}
}
