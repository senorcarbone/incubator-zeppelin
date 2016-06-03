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

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Interpreter for Kompics (http://kompics.sics.se/)
 */
public class KompicsInterpreter extends Interpreter {
	
	Logger logger = LoggerFactory.getLogger(KompicsInterpreter.class);

	static {
		Interpreter.register("kompics", "kompics", KompicsInterpreter.class.getName());
	}


	public KompicsInterpreter(Properties property) {
		super(property);
	}
	
	@Override
	public void open() {

	}

	@Override
	public void close() {

	}

	@Override
	public InterpreterResult interpret(String line, InterpreterContext context) {
		if (line == null || line.trim().length() == 0) {
			return new InterpreterResult(Code.SUCCESS);
		}

		InterpreterResult result = interpret(line.split("\n"), context);
		return result;
	}

	public InterpreterResult interpret(String[] lines, InterpreterContext context) {
		
		return new InterpreterResult(Code.SUCCESS, "Hello Kompics!");
		
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
