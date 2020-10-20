package com.test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DefineClassLoader extends ClassLoader {

	@Override
	protected Class<?> findClass(String name) {
		String helloClassPath = "file:/Users/qzy/IdeaProjects/Week1/src/com/test/Hello.xlass";
		byte[] classByte = null;
		byte[] newClassByte = null;
		try {
			Path path = Paths.get(new URI(helloClassPath));
			classByte = Files.readAllBytes(path);
			newClassByte = new byte[classByte.length];
			for (int i = 0; i < classByte.length; i++) {
				byte b = classByte[i];
				newClassByte[i] = (byte) (255 - b);
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return defineClass(name, newClassByte, 0, newClassByte.length);
	}
	 
	public static void main(String[] args) throws Exception {
		Class<?> classLoaderClass = new DefineClassLoader().findClass("Hello");
		Object object = classLoaderClass.newInstance();
		Method method = classLoaderClass.getMethod("hello");
		method.invoke(object);
	}
}
