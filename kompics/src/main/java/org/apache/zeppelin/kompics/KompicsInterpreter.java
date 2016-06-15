/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zeppelin.kompics;

import org.apache.zeppelin.interpreter.Interpreter;
import org.apache.zeppelin.interpreter.InterpreterContext;
import org.apache.zeppelin.interpreter.InterpreterResult;
import org.apache.zeppelin.interpreter.InterpreterResult.Code;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Console;
import scala.Some;
import scala.runtime.AbstractFunction0;
import scala.tools.nsc.Settings;
import scala.tools.nsc.interpreter.IMain;
import scala.tools.nsc.interpreter.Results;
import scala.tools.nsc.settings.MutableSettings;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Interpreter for Kompics (http://kompics.sics.se/)
 */
public class KompicsInterpreter extends Interpreter {

	Logger logger = LoggerFactory.getLogger(KompicsInterpreter.class);
	private ByteArrayOutputStream out;

	static {
		Interpreter.register("kompics", "kompics", KompicsInterpreter.class.getName());
	}

	private IMain imain;


	public KompicsInterpreter(Properties property) {
		super(property);
	}

	@Override
	public void open() {
		out = new ByteArrayOutputStream();
		imain = new IMain(createSettings());
		resetInterpreter();
	}

	private void resetInterpreter() {
		System.setOut(new PrintStream(out));
		out.reset();
		imain.reset();
		Results.Result res = imain.interpret(
				"import se.sics.kompics.sl._\n" +
						"import se.sics.kompics.{ Start, Kompics, KompicsEvent }");

		if (getResultCode(res) == Code.ERROR) {
			throw new RuntimeException("Failed to initialize the kompics interpreter");
		}
	}

	@Override
	public void close() {
		try {
			imain.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<File> classPath(ClassLoader cl) {
		List<File> paths = new LinkedList<File>();
		if (cl == null) {
			return paths;
		}

		if (cl instanceof URLClassLoader) {
			URLClassLoader ucl = (URLClassLoader) cl;
			URL[] urls = ucl.getURLs();
			if (urls != null) {
				for (URL url : urls) {
					paths.add(new File(url.getFile()));
				}
			}
		}
		return paths;
	}

	private List<File> currentClassPath() {
		List<File> paths = classPath(Thread.currentThread().getContextClassLoader());
		String[] cps = System.getProperty("java.class.path").split(File.pathSeparator);
		if (cps != null) {
			for (String cp : cps) {
				paths.add(new File(cp));
			}
		}
		return paths;
	}

	private Settings createSettings() {
		URL[] urls = getClassloaderUrls();
		Settings settings = new Settings();

		// set classpath
		MutableSettings.PathSetting pathSettings = settings.classpath();
		String classpath = "";
		List<File> paths = currentClassPath();
		for (File f : paths) {
			if (classpath.length() > 0) {
				classpath += File.pathSeparator;
			}
			classpath += f.getAbsolutePath();
		}

		if (urls != null) {
			for (URL u : urls) {
				if (classpath.length() > 0) {
					classpath += File.pathSeparator;
				}
				classpath += u.getFile();
			}
		}

		pathSettings.v_$eq(classpath);
		settings.scala$tools$nsc$settings$ScalaSettings$_setter_$classpath_$eq(pathSettings);
		settings.explicitParentLoader_$eq(new Some<ClassLoader>(Thread.currentThread()
				.getContextClassLoader()));
		MutableSettings.BooleanSetting b = (MutableSettings.BooleanSetting) settings.usejavacp();
		b.v_$eq(true);
		settings.scala$tools$nsc$settings$StandardScalaSettings$_setter_$usejavacp_$eq(b);

		return settings;
	}

	@Override
	public InterpreterResult interpret(final String codeStr, InterpreterContext context) {
		
		resetInterpreter();
		
		if (codeStr == null || codeStr.trim().length() == 0) {
			return new InterpreterResult(Code.SUCCESS);
		}
		
		Results.Result res = Console.withOut(
				System.out,
				new AbstractFunction0<Results.Result>() {
					@Override
					public Results.Result apply() {
						return imain.interpret(codeStr);
					}
				});

		return new InterpreterResult(getResultCode(res), out.toString());
	}

	private Code getResultCode(scala.tools.nsc.interpreter.Results.Result r) {
		if (r instanceof scala.tools.nsc.interpreter.Results.Success$) {
			return Code.SUCCESS;
		} else if (r instanceof scala.tools.nsc.interpreter.Results.Incomplete$) {
			return Code.INCOMPLETE;
		} else {
			return Code.ERROR;
		}
	}

	@Override
	public void cancel(InterpreterContext context) {
	}

	@Override
	public FormType getFormType() {
		return FormType.NATIVE;
	}

	@Override
	public int getProgress(InterpreterContext context) {
		return 0;
	}

	@Override
	public List<String> completion(String buf, int cursor) {
		return new LinkedList<String>();
	}

}
