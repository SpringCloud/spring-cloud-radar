package com.ppdai.infrastructure.radar.client.http.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.TargetClassAware;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.ConfigurableObjectInputStream;
import org.springframework.core.NestedIOException;
import org.springframework.stereotype.Component;

@Component
public class BeanUtils implements ApplicationContextAware {
	private final static Logger logger = LoggerFactory.getLogger(BeanUtils.class);
	private static ApplicationContext applicationContext;
	public static Class<?> getImplClassFromBean(Object bean) {
		if (TargetClassAware.class.isInstance(bean)) {
			return ((TargetClassAware) bean).getTargetClass();
		}

		return bean.getClass();
	}
	public static <T> T clone(Object t,Class<T> class1){
		if(t==null){
			return null;
		}
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1024);
		try {
			serialize(t, byteStream);
		} catch (IOException e) {
			logger.error("serialize 错误",e);
			return null;
		}
		byte[] bts= byteStream.toByteArray();		
		ByteArrayInputStream byteInStream = new ByteArrayInputStream(bts);
		try{
			Object target=deserialize(byteInStream);
			return (T)target;
		}catch (IOException e) {
			logger.error("deserialize 错误",e);
			return null;
		}finally {
			try {
				byteInStream.close();
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
	}
	private static Object deserialize(InputStream inputStream) throws IOException {
		ObjectInputStream objectInputStream = new ConfigurableObjectInputStream(inputStream, BeanUtils.class.getClassLoader());
		try {
			return objectInputStream.readObject();
		}
		catch (ClassNotFoundException ex) {
			logger.error("Failed to deserialize object type", ex);
			throw new NestedIOException("Failed to deserialize object type", ex);
		}
	}
	private static void serialize(Object object, OutputStream outputStream) throws IOException {
		if (!(object instanceof Serializable)) {
			logger.error(object.getClass().getSimpleName() + " requires a Serializable payload " +
					"but received an object of type [" + object.getClass().getName() + "]");
			throw new IllegalArgumentException(object.getClass().getSimpleName() + " requires a Serializable payload " +
					"but received an object of type [" + object.getClass().getName() + "]");
		}
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
		objectOutputStream.writeObject(object);
		objectOutputStream.flush();
	}
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		BeanUtils.applicationContext=applicationContext;
	}

	public static ApplicationContext getContext(){
		return applicationContext;
	}
}
